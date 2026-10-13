import { Injectable } from '@angular/core';
import { AbstractControl, FormGroup } from '@angular/forms';
import { Plan } from '@app/core/model/plan/plan';
import { Guid } from '@common/types/guid';
import { BaseSection } from '@app/core/model/description-template/description-template';
import { SystemFieldType } from '@app/core/common/enum/system-field-type';
import { PlanEditorForm } from './plan-editor.model';
import { PlanLockService } from './plan-lock.service';
import { PlanTempStorageService } from './plan-temp-storage.service';

/**
 * EditorSessionState (FE-0a, target-design §2a).
 *
 * Single per-entity source of truth for the combined Plan/Description editor. It exposes, for the
 * plan and for every loaded description, the same small fact bundle ({ isNew, dirty, valid, status,
 * lock }) so that the navigation guard, the save-set, the TOC status dashboard, the contextual
 * disabled-reason tooltips and the finalize-readiness list all read from ONE derivation instead of
 * each re-deriving entity facts (the current quadruplicated-logic trap).
 *
 * Design notes:
 * - `dirty` is derived from each entity's own `form.dirty`, NOT from `PlanLockService.editLockIds`.
 *   Locks remain for concurrency control but no longer define *what is dirty* / *what gets saved* /
 *   *what blocks navigation*. This closes the silent-exclusion + unguarded-edit edge documented in
 *   the review (an edit whose lock-acquisition failed was in neither editLockIds nor the save set).
 * - These are plain methods rather than `computed()` signals because `form.dirty` / `form.valid` are
 *   not Angular signals; they are evaluated on demand (canDeactivate, save) and during change
 *   detection (template bindings). The lock/status/isNew facts they read ARE signals, so reactive
 *   consumers (FE-2 dashboard) re-evaluate as those change.
 * - FE-0a is additive and dormant: this service is provided and attached, but no consumer is rewired
 *   to it yet. FE-0b rewires canDeactivate / enableSave / isDirty / the save-set onto it.
 *
 * This service is component-scoped (provided on PlanEditorComponent) alongside PlanLockService and
 * PlanTempStorageService, which it injects directly.
 */
@Injectable()
export class EditorSessionStateService {

    private ctx: EditorSessionContext | null = null;

    constructor(
        private planLockService: PlanLockService,
        private planTempStorage: PlanTempStorageService,
    ) {}

    /**
     * Wire the service to the host component's live state. Called from prepareForm/buildForm whenever
     * the plan form is (re)built. Uses thunks so the service always reads current component state
     * without needing to be re-attached on every change.
     */
    public attach(ctx: EditorSessionContext): void {
        this.ctx = ctx;
    }

    public detach(): void {
        this.ctx = null;
    }

    // ---- Plan ---------------------------------------------------------------

    public planState(): EntityState | null {
        if (!this.ctx) { return null; }
        const form = this.ctx.getPlanForm();
        const item = this.ctx.getItem();
        const id = item?.id?.toString() ?? null;
        return {
            id,
            kind: 'plan',
            isNew: this.ctx.getIsNew(),
            dirty: this.isFormDirty(form),
            valid: this.isFormValid(form),
            status: this.toStatusInfo(item?.status),
            lock: this.lockInfoFor(id),
        };
    }

    public isPlanDirty(): boolean {
        return !!this.ctx && this.isFormDirty(this.ctx.getPlanForm());
    }

    /**
     * FE-2: per-section dirtiness for the PLAN body. The plan is a single form, so we attribute
     * dirtiness to a blueprint section by inspecting the value controls of its fieldSets
     * (properties.fieldSets[id]) and the plan-level controls behind its system fields
     * (title/description/language/access/contacts). Without this, any plan edit marks the whole form
     * dirty and every section would read as "unsaved".
     */
    public planSectionDirty(sectionId: string): boolean {
        return this.sectionControls(sectionId).some(c => c.dirty);
    }

    /**
     * FE-2: per-section "incomplete" for the PLAN body — true if any of the section's controls is
     * invalid. Validity is visibility-aware (hidden required fields neutralise their required
     * validator), so this reflects genuinely-pending required/invalid fields. Used for the proactive
     * (subtle) section indicator and the finalize dialog's "sections needing input" list.
     */
    public planSectionIncomplete(sectionId: string): boolean {
        return this.sectionControls(sectionId).some(c => c.invalid);
    }

    /** The plan-form controls that belong to a blueprint section (its fieldSets + system fields). */
    private sectionControls(sectionId: string): AbstractControl[] {
        if (!this.ctx || !sectionId) { return []; }
        const form = this.ctx.getPlanForm();
        const section = this.findSection(this.ctx.getItem()?.blueprint?.definition?.sections as BaseSection[], sectionId);
        if (!form || !section) { return []; }
        const controls: AbstractControl[] = [];
        const fieldSetsGroup = form.get('properties')?.get('fieldSets');
        for (const fsId of this.collectFieldSetIds(section)) {
            const c = fieldSetsGroup?.get(fsId);
            if (c) { controls.push(c); }
        }
        for (const sysType of this.collectSystemFieldTypes(section)) {
            const c = this.systemFieldControl(form, sysType);
            if (c) { controls.push(c); }
        }
        return controls;
    }

    private findSection(sections: BaseSection[] | undefined, id: string): BaseSection | null {
        for (const s of sections ?? []) {
            if (s.id === id) { return s; }
            const nested = this.findSection(s.sections, id);
            if (nested) { return nested; }
        }
        return null;
    }

    private collectFieldSetIds(section: BaseSection): string[] {
        const ids: string[] = (section.fieldSets ?? []).map(fs => fs.id);
        (section.sections ?? []).forEach(s => ids.push(...this.collectFieldSetIds(s)));
        return ids;
    }

    private collectSystemFieldTypes(section: BaseSection): SystemFieldType[] {
        const types: SystemFieldType[] = [];
        // Fields are description-template-shaped here (rendered via app-form-section), so the system
        // field type lives at field.data.systemFieldType — NOT a top-level field.systemFieldType.
        (section.fieldSets ?? []).forEach(fs => (fs.fields ?? []).forEach((f: any) => {
            const sysType = f?.data?.systemFieldType;
            if (sysType != null) { types.push(sysType); }
        }));
        (section.sections ?? []).forEach(s => types.push(...this.collectSystemFieldTypes(s)));
        return types;
    }

    private systemFieldControl(form: FormGroup, sysType: SystemFieldType): AbstractControl | null {
        switch (sysType) {
            case SystemFieldType.PlanTitle: return form.get('label');
            case SystemFieldType.PlanDescription: return form.get('description');
            case SystemFieldType.PlanLanguage: return form.get('language');
            case SystemFieldType.PlanAccessRights: return form.get('accessType');
            case SystemFieldType.PlanContact: return form.get('properties')?.get('contacts') ?? null;
            default: return null;
        }
    }

    // ---- Descriptions -------------------------------------------------------

    public descriptionState(id: Guid | string): EntityState | null {
        const idStr = id?.toString();
        if (!idStr) { return null; }
        const info = this.planTempStorage.descriptions()?.get(idStr);
        if (!info) { return null; }
        const isNew = !!info.isNew || (this.ctx?.getNewDescriptionIds()?.includes(idStr) ?? false);
        return {
            id: idStr,
            kind: 'description',
            isNew,
            // A brand-new (unsaved) description is always "dirty" — it must be persisted even if its
            // form has not fired a change event yet.
            dirty: isNew || this.isFormDirty(info.formGroup),
            valid: this.isFormValid(info.formGroup),
            status: this.toStatusInfo(info.lastPersist?.status),
            lock: this.lockInfoFor(idStr),
        };
    }

    /** Per-description state for every loaded description, keyed by id. */
    public descriptionStates(): Map<string, EntityState> {
        const result = new Map<string, EntityState>();
        this.planTempStorage.descriptions()?.forEach((_info, idStr) => {
            const state = this.descriptionState(idStr);
            if (state) { result.set(idStr, state); }
        });
        return result;
    }

    /**
     * Ids of descriptions that need persisting = own form is dirty OR the description is new.
     * This replaces the old editLockIds-based save-set derivation (FE-0b consumes this).
     */
    public dirtyDescriptionIds(): string[] {
        const ids: string[] = [];
        this.descriptionStates().forEach((state, idStr) => {
            if (state.dirty) { ids.push(idStr); }
        });
        return ids;
    }

    // ---- Aggregate ----------------------------------------------------------

    public isAnythingDirty(): boolean {
        return this.isPlanDirty() || this.dirtyDescriptionIds().length > 0;
    }

    public snapshot(): EditorSessionState {
        return {
            plan: this.planState(),
            descriptions: this.descriptionStates(),
        };
    }

    // ---- Locks --------------------------------------------------------------

    /**
     * Map the lock signals onto a single per-entity lock state.
     * FE-1: `'lost'` (a lock that was held by this user and then lost/failed mid-edit) is distinguished
     * from `'held-by-other'` (never mine), and `owner` provenance (who holds it / since when) is
     * populated from PlanLockService.lockOwners.
     */
    public lockInfoFor(id: Guid | string): EntityLockInfo {
        const idStr = id?.toString();
        if (!idStr) { return { state: 'none' }; }
        if (this.planLockService.editLockIds()?.includes(idStr) || this.planLockService.transientLockId() === idStr) {
            return { state: 'held-by-me' };
        }
        const ownerRaw = this.planLockService.lockOwners()?.get(idStr);
        const owner = ownerRaw ? { name: ownerRaw.name, since: ownerRaw.at } : undefined;
        if (this.planLockService.lostLockIds()?.includes(idStr)) {
            return { state: 'lost', owner };
        }
        if (this.planLockService.externalLockIds()?.includes(idStr)) {
            return { state: 'held-by-other', owner };
        }
        return { state: 'none' };
    }

    // ---- Helpers ------------------------------------------------------------

    private isFormDirty(form: FormGroup | null | undefined): boolean {
        return !!form && form.dirty;
    }

    /**
     * "valid" here mirrors the existing finalize semantics: a disabled (read-only / locked) form is
     * treated as valid because we cannot know otherwise and must not block on it. See
     * plan-finalize-dialog.component.ts and descriptionBaseValid in the editor.
     */
    private isFormValid(form: FormGroup | null | undefined): boolean {
        return !!form && (form.disabled || form.valid);
    }

    private toStatusInfo(status: StatusLike | null | undefined): EntityStatusInfo | undefined {
        if (!status) { return undefined; }
        return {
            id: status.id?.toString(),
            internalStatus: status.internalStatus,
            name: status.name,
            statusColor: status.definition?.statusColor,
        };
    }
}

// ---- Types ------------------------------------------------------------------

export type EntityKind = 'plan' | 'description';

/** `'lost'` is reserved for FE-1 (a lock held-by-me that was lost mid-edit). */
export type LockState = 'none' | 'held-by-me' | 'held-by-other' | 'lost';

export interface EntityLockInfo {
    state: LockState;
    /** Populated in FE-1 from the server-side Lock.lockedBy / lockedAt. */
    owner?: { name?: string; since?: Date };
}

export interface EntityStatusInfo {
    id?: string;
    /** PlanStatusEnum | DescriptionStatusEnum value. */
    internalStatus?: number;
    name?: string;
    statusColor?: string;
}

export interface EntityState {
    id: string | null;
    kind: EntityKind;
    isNew: boolean;
    dirty: boolean;
    valid: boolean;
    status?: EntityStatusInfo;
    lock: EntityLockInfo;
}

export interface EditorSessionState {
    plan: EntityState | null;
    descriptions: Map<string, EntityState>;
}

/** Live references the host component supplies via attach(). */
export interface EditorSessionContext {
    getPlanForm: () => FormGroup<PlanEditorForm>;
    getItem: () => Plan;
    getIsNew: () => boolean;
    getNewDescriptionIds: () => string[];
}

/** Minimal shape shared by PlanStatus and DescriptionStatus. */
interface StatusLike {
    id?: Guid | string;
    internalStatus?: number;
    name?: string;
    definition?: { statusColor?: string };
}

/**
 * FE-2: pick a readable text color (black/white) for a given admin-configured status background color,
 * so the status chip stays legible whatever color the admin chose. Handles #rgb / #rrggbb hex; returns
 * null for unknown formats (or no color) so the CSS default applies.
 */
export function statusContrastColor(bg: string | null | undefined): string | null {
    if (!bg) { return null; }
    const hex = bg.trim().replace('#', '');
    let r: number, g: number, b: number;
    if (hex.length === 3) {
        r = parseInt(hex[0] + hex[0], 16); g = parseInt(hex[1] + hex[1], 16); b = parseInt(hex[2] + hex[2], 16);
    } else if (hex.length === 6) {
        r = parseInt(hex.substring(0, 2), 16); g = parseInt(hex.substring(2, 4), 16); b = parseInt(hex.substring(4, 6), 16);
    } else {
        return null;
    }
    if ([r, g, b].some(v => isNaN(v))) { return null; }
    // perceived luminance (0..1)
    const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
    return luminance > 0.6 ? 'rgba(0,0,0,0.87)' : '#fff';
}
