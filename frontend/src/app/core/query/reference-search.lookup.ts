import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";
import { Reference } from "../model/reference/reference";

export class ReferenceSearchLookup extends Lookup {
	like: string;
	typeId: Guid;
	key: string;
	dependencyReferences: Reference[];

	constructor() {
		super();
	}
}


export class ReferenceSearchDefinitionLookup extends Lookup {
	like: string;
	typeId: Guid;
	key: string;
	dependencyReferences: Reference[];

	constructor() {
		super();
	}
}