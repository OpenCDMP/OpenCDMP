import { Component, effect, EventEmitter, input, Input, Output } from '@angular/core';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { BaseComponent } from '@common/base/base.component';
import { Subscription } from 'rxjs';
import { FormService } from '@common/forms/form-service';
import { ExternalFetcherBaseSourceConfigurationPersist } from '@app/core/model/external-fetcher/external-fetcher';
import { Prefilling } from '@app/core/model/prefilling-source/prefilling-source';
import { PrefillingSourceService } from '@app/core/services/prefilling-source/prefilling-source.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'app-prefilling-field-component',
    templateUrl: './prefilling-field.component.html',
    styleUrls: ['./prefilling-field.component.scss'],
    standalone: false
})
export class PrefillingFieldComponent extends BaseComponent {
	sources = input<ExternalFetcherBaseSourceConfigurationPersist []>();
	definitionSourcekey = input<string>();

	@Output() selectedPrefilling: EventEmitter<any> = new EventEmitter();

	singleAutoCompleteSearchConfiguration: SingleAutoCompleteConfiguration;

	dependenciesSubscription: Subscription = null;
	constructor(
		protected language: TranslateService,	
		private prefillingService: PrefillingSourceService,
		public enumUtils: EnumUtils,
		public formService: FormService,
	) { 
        super();
        effect(() => {
            const sources = this.sources();

            if(sources?.length){
                this.resetAutocompleteConfiguration();
            }
        })
    }

    resetAutocompleteConfiguration(){
		if(this.sources()?.length)
            this.singleAutoCompleteSearchConfiguration = this.prefillingService.getSingleAutocompleteTestConfiguration(this.definitionSourcekey(), this.sources())
    }
	
	onOptionSelected(selectedOption: Prefilling) {
		this.selectedPrefilling.emit(selectedOption);
	}
}
