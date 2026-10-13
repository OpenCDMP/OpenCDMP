import { Lookup } from "@common/model/lookup";
import { Reference } from "../model/reference/reference";
import { ExternalFetcherBaseSourceConfigurationPersist } from "../model/external-fetcher/external-fetcher";

export class ReferenceTestLookup extends Lookup {
	like: string;
	key: string;
	dependencyReferences: Reference[];
	sources: ExternalFetcherBaseSourceConfigurationPersist[];

	constructor() {
		super();
	}
}
