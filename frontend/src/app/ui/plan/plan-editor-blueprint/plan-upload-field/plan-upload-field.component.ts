
import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MatDialog } from "@angular/material/dialog";
import { FieldInSection, UploadFieldInSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { DescriptionService } from '@app/core/services/description/description.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { TagService } from '@app/core/services/tag/tag.service';
import { FileUploadComponent } from '@app/library/file-uploader/file-uploader.component';
import { BaseComponent } from '@common/base/base.component';
import { Observable } from 'rxjs';

@Component({
	selector: 'app-plan-upload-field',
	templateUrl: './plan-upload-field.component.html',
	styleUrls: ['./plan-upload-field.component.scss'],
    imports: [CommonModule, FileUploadComponent]
})
export class PlanUploadFieldComponent extends BaseComponent {

	@Input() field: FieldInSection;
	@Input() form: UntypedFormGroup;
	@Input() detectChangesObservable: Observable<any>;
    @Input() path: string;

	fileNameDisplay: string = null;

	constructor(
		public planService: PlanService,
		public descriptionService: DescriptionService,
		public tagService: TagService,
		public dialog: MatDialog,
	) {
		super();
	}

    get maxFileSize(): number { 
        const data = this.field as UploadFieldInSection;
        return data.maxFileSizeInMB ? data.maxFileSizeInMB * 1048576 : null; //from MB to bytes
    }

    typesToString() {
		return (this.field as UploadFieldInSection)?.types?.map(type => type.value)?.toString();
	}
}
