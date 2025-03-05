import { Component, Input, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { PrefillingSourceSystemTargetType } from '@app/core/common/enum/prefilling-source-system-target-type';
import { SemanticsService } from '@app/core/services/semantic/semantics.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';

@Component({
    selector: 'app-prefilling-source-field-component',
    templateUrl: 'prefilling-source-field.component.html',
    styleUrls: ['./prefilling-source-field.component.scss'],
    standalone: false
})
export class PrefillingSourceComponent extends BaseComponent implements OnInit {

	@Input() form: UntypedFormGroup = null;

	prefillingSourceSystemTargetTypeEnumValues = this.enumUtils.getEnumValues<PrefillingSourceSystemTargetType>(PrefillingSourceSystemTargetType);

	constructor(
		public enumUtils: EnumUtils,
		public semanticsService: SemanticsService,
	) { super(); }

	ngOnInit() {
	}

}
