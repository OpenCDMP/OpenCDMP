import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ChangeDetectionStrategy} from "@angular/core";
import { PlanBlueprint } from "@app/core/model/plan-blueprint/plan-blueprint";
import {BaseComponent} from "@common/base/base.component";
import {HttpErrorHandlingService} from "@common/modules/errors/error-handling/http-error-handling.service";
import { EnumUtils } from "@app/core/services/utilities/enum-utils.service";
import { MatCheckbox } from "@angular/material/checkbox";

@Component({
    selector: 'app-plan-creation-consent-component',
    templateUrl: './plan-creation-consent.component.html',
    styleUrls: ['./plan-creation-consent.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatCheckbox]
})

export class PlanCreationConsentComponent extends BaseComponent implements OnChanges {
	@Input() public selectedBlueprint: PlanBlueprint = null; // selected blueprint
	@Output() planCreationConsentChange: EventEmitter<boolean> = new EventEmitter<boolean>();

	checked = false;

	constructor(
        protected httpErrorHandlingService: HttpErrorHandlingService,
        protected enumUtils: EnumUtils
    ) {
		super();
	}

	ngOnChanges(changes: SimpleChanges): void {
		if (changes['selectedBlueprint']) {
			this.checked = false;
			this.planCreationConsentChange.emit(false);
		}
	}

	onChange(checked: boolean): void {
		this.checked = checked;
		this.planCreationConsentChange.emit(checked);
	}

}
