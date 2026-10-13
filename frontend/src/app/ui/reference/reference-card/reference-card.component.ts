import { Component, computed, DestroyRef, effect, inject, input, OnDestroy, output, signal, ChangeDetectionStrategy } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { ReferenceSourceType } from '@app/core/common/enum/reference-source-type';
import { Reference } from '@app/core/model/reference/reference';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { MaterialModule } from '@common/material/material.module';
import { TranslateModule } from '@ngx-translate/core';

// Self-describing reference display: source logo + label + identifier + source badge, with an
// optional expandable list of the reference's dynamic fields. Shared by the editor (compact, as a
// chip) and the read-only views (full, expandable). Resolves the logo/source/field-labels from the
// reference type definition (preferring the explicit [referenceType] input, else reference.type).
import { Guid } from '@common/types/guid';

@Component({
    selector: 'app-reference-card',
    standalone: true,
    imports: [MaterialModule, TranslateModule],
    templateUrl: './reference-card.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrls: ['./reference-card.component.scss']
})
export class ReferenceCardComponent implements OnDestroy {
    reference = input<Reference>();
    referenceType = input<ReferenceType>();
    compact = input<boolean>(false);
    dense = input<boolean>(false);
    showDescription = input<boolean>(false);
    expandable = input<boolean>(true);   // set false where expansion doesn't fit (e.g. dropdown options)
    bordered = input<boolean>(true);     // set false when the host supplies the chrome (e.g. mat-option)
    canRemove = input<boolean>(false);
    remove = output<void>();
    canEdit = input<boolean>(false);
    edit = output<void>();

    // Optional secondary line (e.g. to disambiguate same-name references in a picker).
    protected descriptionText = computed(() => this.reference()?.description?.trim() || null);

    // Clean name for display: strip a trailing "(...)" when it's an identifier rather than meaningful
    // text — i.e. it contains the reference id, a "scheme:value" / URL (e.g. "(orcid:0000-...)"), or
    // is a bare identifier (digits/dashes). Real parentheticals like "(Main Campus)" are kept. The
    // full label is still available as a tooltip.
    protected displayName = computed(() => {
        const ref = this.reference();
        const label = (ref?.label ?? '').trim();
        if (!label.endsWith(')')) { return label; }
        const open = label.lastIndexOf('(');
        if (open < 0) { return label; }
        const inner = label.slice(open + 1, -1).trim();
        const id = ref?.reference;
        const looksLikeId =
            (!!id && inner.includes(id)) ||
            /:/.test(inner) ||                         // scheme:value or URL
            /^[0-9xX][0-9xX\-\s.\/]*$/.test(inner);    // bare identifier (digits/dashes)
        if (looksLikeId) {
            const stripped = label.slice(0, open).trim();
            return stripped || label;
        }
        return label;
    });

    private titleCase(s: string): string {
        return (s ?? '').replace(/\w\S*/g, t => t.charAt(0).toUpperCase() + t.slice(1).toLowerCase());
    }

    // Some imported references carry a non-meaningful machine identifier instead of a citable one,
    // left behind by a faulty file-transformer import. Two shapes seen in the wild:
    //   - a Java object's default toString:      "org.opencdmp...ContributorId@f4fa087"
    //   - an internal placeholder id:            "pending_org_::32309ab5473067838f2b73820f5469b5"
    // sometimes wrapped as a Java list/array toString ("[org...@f4fa087, ...]"). Hide those — they
    // are noise. Real identifiers (ORCID, DOI, ROR, emails, URLs) don't match these shapes.
    private static readonly MACHINE_REF_PATTERNS = [
        /^[a-zA-Z_$][\w$]*(\.[a-zA-Z_$][\w$]*)+@[0-9a-fA-F]+$/,   // dotted.ClassName@hexhash
        /^[\w.-]+::[0-9a-fA-F]{8,}$/,                              // prefix::longhex placeholder
    ];
    protected displayId = computed(() => {
        const raw = this.reference()?.reference?.trim();
        if (!raw) { return null; }
        // Manual references get a generated GUID as their code; it identifies nothing to the user.
        if (this.isInternal() && Guid.isGuid(raw)) { return null; }
        const inner = raw.replace(/^\[+|\]+$/g, '').trim();   // unwrap Java list/array toString
        const tokens = inner.split(',').map(t => t.trim()).filter(t => t.length);
        const isMachine = (t: string) => ReferenceCardComponent.MACHINE_REF_PATTERNS.some(p => p.test(t));
        const allMachine = tokens.length > 0 && tokens.every(isMachine);
        return allMachine ? null : raw;
    });

    private storageFileService = inject(StorageFileService);
    private sanitizer = inject(DomSanitizer);
    private destroyRef = inject(DestroyRef);

    protected expanded = signal(false);
    protected logoUrl = signal<SafeUrl | null>(null);
    private objectUrl: string | null = null;
    private loadedLogoId: string = null;

    // Type definition (field labels + source configs); prefer the explicit input, fall back to the
    // reference's embedded type (may be shallow), so the card degrades gracefully either way.
    private typeDefinition = computed(() => this.referenceType()?.definition ?? this.reference()?.type?.definition);

    protected sourceConfig = computed(() => {
        const ref = this.reference();
        return this.typeDefinition()?.sources?.find(s => s.key === ref?.source) ?? null;
    });

    protected isInternal = computed(() => this.reference()?.sourceType === ReferenceSourceType.Internal);

    // Badge text: the configured source label, else the title-cased raw source key. Title-casing
    // normalizes the inconsistent stored casing ("internal" / "Internal" -> "Internal").
    protected sourceLabel = computed(() => this.sourceConfig()?.label ?? this.titleCase(this.reference()?.source ?? ''));

    // Deep link to the external record. The source's navigationUrl is a template with {fieldCode}
    // placeholders (system codes reference_id/label/description + dynamic field codes); resolve them
    // from this reference's values. Returns null if there's no template or any placeholder is unresolved.
    protected navigationUrl = computed(() => {
        const template = this.sourceConfig()?.navigationUrl;
        const ref = this.reference();
        if (!template || !ref || this.isInternal()) { return null; }
        const values = new Map<string, string>();
        values.set('reference_id', ref.reference);
        values.set('label', ref.label);
        values.set('description', ref.description);
        (ref.definition?.fields ?? []).forEach(f => { if (f.code != null) { values.set(f.code, f.value); } });
        const resolved = template.replace(/\{([\w-]+)\}/g, (_m, code) => {
            const v = values.get(code);
            return v != null ? encodeURIComponent(v) : `{${code}}`;
        });
        return /\{[\w-]+\}/.test(resolved) ? null : resolved;
    });

    // Dynamic field values with resolved human labels, excluding internal codes and empty values.
    protected fields = computed(() => {
        const values = this.reference()?.definition?.fields ?? [];
        const defs = this.typeDefinition()?.fields ?? [];
        return values
            .filter(f => f.code !== 'key' && f.code !== 'referenceType' && f.value != null && f.value !== '')
            .map(f => ({ label: defs.find(d => d.code === f.code)?.label ?? f.code, value: f.value }));
    });

    constructor() {
        effect(() => {
            const id = this.sourceConfig()?.storageFile?.id;
            if (id && id.toString() !== this.loadedLogoId) {
                this.loadedLogoId = id.toString();
                this.downloadLogo(id);
            }
        });
    }

    private downloadLogo(id: any) {
        this.storageFileService.download(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(response => {
            this.revokeLogo();
            this.objectUrl = URL.createObjectURL(response.body);
            this.logoUrl.set(this.sanitizer.bypassSecurityTrustUrl(this.objectUrl));
        });
    }

    private revokeLogo() {
        if (this.objectUrl) { URL.revokeObjectURL(this.objectUrl); this.objectUrl = null; }
    }

    ngOnDestroy(): void {
        this.revokeLogo();
    }
}
