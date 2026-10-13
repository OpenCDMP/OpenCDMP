import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { MatIconModule } from '@angular/material/icon';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { templateTypeStyle } from './template-type-style';

/**
 * Rounded icon tile for an asset template: the template's configured logo or Material icon,
 * tinted with the colour pair derived from the template type.
 */
@Component({
	selector: 'app-template-type-icon',
	imports: [MatIconModule],
	changeDetection: ChangeDetectionStrategy.OnPush,
	template: `
		@let s = style();
		<span class="type-icon" [class.type-icon--small]="size() === 'small'" [style.background-color]="s.background" [style.color]="s.color" aria-hidden="true">
			@if (logoUrl()) {
				<img alt="" [src]="logoUrl()" />
			} @else {
				<mat-icon class="material-symbols-outlined">{{ template()?.definition?.iconConfiguration?.matIconName || 'description' }}</mat-icon>
			}
		</span>
	`,
	styles: `
		.type-icon {
			display: inline-flex;
			align-items: center;
			justify-content: center;
			flex-shrink: 0;
			width: 44px;
			height: 44px;
			border-radius: 12px;
			&.type-icon--small { width: 32px; height: 32px; border-radius: 8px; mat-icon { font-size: 18px; width: 18px; height: 18px; } img { width: 18px; height: 18px; } }
			mat-icon { font-size: 22px; width: 22px; height: 22px; }
			img { width: 24px; height: 24px; object-fit: contain; }
		}
	`,
})
export class TemplateTypeIconComponent {
	private readonly storageFileService = inject(StorageFileService);
	private readonly sanitizer = inject(DomSanitizer);

	template = input<DescriptionTemplate | null | undefined>();
	size = input<'default' | 'small'>('default');

	protected style = computed(() => templateTypeStyle(this.template()?.type));
	protected logoUrl = signal<SafeUrl | null>(null);

	constructor() {
		effect(() => {
			const fileId = this.template()?.definition?.iconConfiguration?.storageFile?.id;
			this.logoUrl.set(null);
			if (!fileId) { return; }
			this.storageFileService.download(fileId).subscribe({
				next: response => this.logoUrl.set(this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(response.body))),
				error: () => this.logoUrl.set(null),
			});
		});
	}
}
