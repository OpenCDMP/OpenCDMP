import { FormControl } from '@angular/forms';
import { BaseComponent } from '@common/base/base.component';

export abstract class TenantConfigurationBaseComponent extends BaseComponent {

	readonly secretValue: string = '1784E159-C809-4F3F-AC8F-90F98B04B01D';
	shouldShowEditSecret: Map<string, boolean> = new Map<string, boolean>();

	protected constructor() { super(); }

	editSecret(formControl: FormControl, formControlName: string) {
		this.shouldShowEditSecret.set(formControlName, true);
		formControl.setValue(undefined);
	}

	cancelEditSecret(formControl: FormControl, formControlName: string) {
		this.shouldShowEditSecret.delete(formControlName);
		formControl.setValue(this.secretValue);
	}
}
