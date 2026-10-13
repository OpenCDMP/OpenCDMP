import { Component, Inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { DescriptionTemplatePreviewDialogComponent } from '@app/ui/admin/description-template/description-template-preview/description-template-preview-dialog.component';
import { BaseComponent } from '@common/base/base.component';
import { Guid } from '@common/types/guid';
import { debounceTime, distinctUntilChanged, startWith, switchMap, takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { MatFormField, MatPrefix } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { ReactiveAsteriskDirective } from '../../../../../../../common/forms/reactive-asterisk-directive';
import { MatInput } from '@angular/material/input';
import { MatButton } from '@angular/material/button';
import { TranslatePipe } from '@ngx-translate/core';

/** Multi-select picker for the asset templates a section offers. Only finalized templates, minus the ones already listed. */
@Component({
    selector: 'app-bp2-template-picker-dialog',
    templateUrl: './template-picker.dialog.html',
    styleUrls: ['./template-picker.dialog.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, CdkScrollable, MatDialogContent, MatFormField, MatIcon, MatPrefix, ReactiveAsteriskDirective, MatInput, FormsModule, ReactiveFormsModule, MatButton, MatDialogActions, TranslatePipe]
})
export class TemplatePickerDialogComponent extends BaseComponent implements OnInit {
	search = new FormControl<string>('');
	items: DescriptionTemplate[] = null;
	selected = new Map<string, DescriptionTemplate>();

	constructor(
		public dialogRef: MatDialogRef<TemplatePickerDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: { excludedGroupIds: Guid[] },
		private descriptionTemplateService: DescriptionTemplateService,
		private dialog: MatDialog
	) { super(); }

	ngOnInit(): void {
		this.search.valueChanges.pipe(startWith(''), debounceTime(250), distinctUntilChanged(), switchMap(like => this.descriptionTemplateService.query(this.descriptionTemplateService.buildDescriptionTemplateGroupAutocompleteLookup({
			isActive: [IsActive.Active],
			like: like?.length ? like : null,
			excludedGroupIds: this.data.excludedGroupIds?.length ? this.data.excludedGroupIds : null,
			lookupFields: [
				nameof<DescriptionTemplate>(x => x.id), nameof<DescriptionTemplate>(x => x.label), nameof<DescriptionTemplate>(x => x.groupId),
				nameof<DescriptionTemplate>(x => x.description), nameof<DescriptionTemplate>(x => x.code), nameof<DescriptionTemplate>(x => x.version),
				nameof<DescriptionTemplate>(x => x.status), nameof<DescriptionTemplate>(x => x.belongsToCurrentTenant),
				[nameof<DescriptionTemplate>(x => x.type), 'name'].join('.')
			]
		}))), takeUntil(this._destroyed)).subscribe({ next: result => this.items = result.items ?? [], error: () => this.items = [] });
	}

	isSelected(item: DescriptionTemplate): boolean { return this.selected.has(item.groupId?.toString()); }
	toggle(item: DescriptionTemplate): void { const key = item.groupId?.toString(); if (this.selected.has(key)) this.selected.delete(key); else this.selected.set(key, item); }
	preview(item: DescriptionTemplate, event: Event): void {
		event.stopPropagation();
		this.dialog.open(DescriptionTemplatePreviewDialogComponent, { width: 'min(800px, 95vw)', minHeight: '200px', data: { descriptionTemplateId: item.id }, panelClass: 'custom-modalbox' });
	}
	snippet(item: DescriptionTemplate): string {
		const text = (item.description ?? '').replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim();
		return text.length > 110 ? text.slice(0, 110) + '…' : text;
	}
	confirm(): void { this.dialogRef.close([...this.selected.values()]); }
	cancel(): void { this.dialogRef.close(null); }
}
