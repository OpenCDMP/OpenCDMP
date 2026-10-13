
import { Component, Input, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { FormService } from '@common/forms/form-service';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { BaseComponent } from '@common/base/base.component';
import { IconConfigurationForm } from './icon-configuration-editor.model';
import { MAT_ICON_OPTIONS } from './mat-icon-options';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatFormField, MatLabel, MatError, MatPrefix } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatAutocomplete, MatAutocompleteTrigger } from '@angular/material/autocomplete';
import { MatOption } from '@angular/material/core';
import { ReactiveAsteriskDirective } from '../../../common/forms/reactive-asterisk-directive';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FileUploadComponent } from '../../library/file-uploader/file-uploader.component';
import { takeUntil } from 'rxjs/operators';

// enough to browse without rendering the whole icon set in the panel
const MAX_SUGGESTIONS = 100;

@Component({
    selector: 'app-icon-configuration-editor',
    templateUrl: 'icon-configuration-editor.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatButton, MatIcon, MatFormField, MatLabel, MatPrefix, MatInput, MatAutocomplete, MatAutocompleteTrigger, ReactiveAsteriskDirective, FormsModule, ReactiveFormsModule, MatOption, MatError, FileUploadComponent, TranslatePipe]
})
export class IconConfigurationEditorComponent extends BaseComponent implements OnInit {

	@Input() formGroup: IconConfigurationForm = null;
	isUsingDropzone: boolean = false;
	private readonly matIconSet = new Set<string>(MAT_ICON_OPTIONS);
	private storedIconName: string = null;
	filteredIcons: string[] = MAT_ICON_OPTIONS.slice(0, MAX_SUGGESTIONS);

	constructor(
		protected dialog: MatDialog,
		protected language: TranslateService,
		protected formService: FormService

	) {
		super();
	}

	ngOnInit(): void {
		// start on the icon name unless an image is already uploaded
		this.isUsingDropzone = !!this.formGroup?.controls?.storageFileId.value && !this.formGroup?.controls?.matIconName.value;
		this.storedIconName = this.formGroup?.controls?.matIconName.value;
		this.formGroup?.controls?.matIconName.valueChanges.pipe(takeUntil(this._destroyed)).subscribe(value => this.filterIcons(value));
	}

	isKnownIcon(name: string): boolean {
		return !!name && this.matIconSet.has(name);
	}

	// typed text that isn't an icon name would save as a blank icon, so drop it when the panel closes,
	// or on blur when no suggestion matched and the panel never opened. A stored name outside the list
	// (a custom or older icon) is kept: only text typed here is dropped
	clearUnknownIcon(): void {
		const control = this.formGroup?.controls?.matIconName;
		if (control?.value && control.value !== this.storedIconName && !this.isKnownIcon(control.value)) control.setValue(null);
	}

	private filterIcons(value: string): void {
		const term = (value ?? '').trim().toLowerCase().replace(/\s+/g, '_');
		const matches = term ? MAT_ICON_OPTIONS.filter(x => x.includes(term)) : MAT_ICON_OPTIONS;
		this.filteredIcons = matches.slice(0, MAX_SUGGESTIONS);
	}

	toggleInputMethod(){
        this.isUsingDropzone = !this.isUsingDropzone;
		if (this.isUsingDropzone) this.formGroup?.controls?.matIconName.setValue(null);
		else this.formGroup?.controls?.storageFileId.setValue(null);
    }
}
