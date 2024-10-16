export class Lookup {
	public page: Lookup.Paging;
	public order: Lookup.Ordering;
	public metadata: Lookup.Header;
	public project: Lookup.FieldDirectives;

	constructor() {
		this.project = {
			fields: []
		};
	}
}

export namespace Lookup {
	export interface Header {
		countAll: boolean;
	}

	export interface FieldDirectives {
		fields: string[];
	}

	export interface Ordering {
		items: string[];
	}

	export interface Paging {
		offset: number;
		size: number;
	}
}