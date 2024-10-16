import {ValidatorFn, Validators} from "@angular/forms";

export enum ValidationType {
	None = 0,
	Required = 1,
	URL = 2
}

//TODO: move this
export class ValidatorURL {

	public static regex = 'https?:\/\/(?:www\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.[^\s]{2,}|www\.' +
		'[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.[^\s]{2,}|https?:\/\/(?:www\.|(?!www))[a-zA-Z0-9]+\.[^\s]{2,}|www\.' +
		'[a-zA-Z0-9]+\.[^\s]{2,}';

	public static get validator(): ValidatorFn {
		return Validators.pattern(ValidatorURL.regex);
	}
}
