import { Lookup } from "@common/model/lookup";

export class DefaultUserLocaleTimezoneLookup extends Lookup {
	like: string;
	selectedItem: string;

	constructor() {
		super();
	}
}

export class DefaultUserLocaleCultureLookup extends Lookup {
	like: string;
	selectedItem: string;

	constructor() {
		super();
	}
}