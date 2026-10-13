import { ConnectedPosition, CdkOverlayOrigin, CdkConnectedOverlay } from '@angular/cdk/overlay';
import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatIcon } from '@angular/material/icon';
import { IconConfigurationEditorComponent } from '../../../../iconconfiguration/icon-configuration-editor.component';
import { MatButton } from '@angular/material/button';
import { TranslatePipe } from '@ngx-translate/core';

/** The icon tile on the About card: shows the icon, and opens the icon editor in a popover next to it. */
@Component({
    selector: 'app-se2-icon-picker',
    templateUrl: './icon-picker.component.html',
    styleUrls: ['./icon-picker.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [CdkOverlayOrigin, MatIcon, CdkConnectedOverlay, IconConfigurationEditorComponent, FormsModule, ReactiveFormsModule, MatButton, TranslatePipe]
})
export class IconPickerComponent {
	@Input() iconForm: UntypedFormGroup;
	@Input() readOnly = false;
	open = false;

	readonly positions: ConnectedPosition[] = [
		{ originX: 'start', originY: 'bottom', overlayX: 'start', overlayY: 'top', offsetY: 8 },
		{ originX: 'start', originY: 'top', overlayX: 'start', overlayY: 'bottom', offsetY: -8 }
	];

	get hasIcon(): boolean { return !!this.iconForm?.get('matIconName')?.value || !!this.iconForm?.get('storageFileId')?.value; }

	remove(): void {
		this.iconForm.get('matIconName')?.setValue(null);
		this.iconForm.get('storageFileId')?.setValue(null);
		this.iconForm.markAsDirty();
		this.open = false;
	}

	onKeydown(event: KeyboardEvent): void { if (event.key === 'Escape') this.open = false; }
}
