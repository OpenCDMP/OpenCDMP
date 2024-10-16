import { Injectable } from '@angular/core';
import { Lookup } from '@common/model/lookup';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class QueryParamsService {

	constructor() { }


	serializeLookup(lookup: Lookup): string {
		return JSON.stringify(lookup, (key: string, value: any) => {
			switch (key) {
				case nameof<Lookup>(x => x.metadata):
				case nameof<Lookup>(x => x.project):
					return undefined;
				default:
					return value == null ? undefined : value;
			}
		});
	}

	deSerializeLookup(serializedLookup: string): any {
		const json = JSON.parse(serializedLookup);
		delete json[nameof<Lookup>(x => x.metadata)];
		delete json[nameof<Lookup>(x => x.project)];
		return json;
	}


	serializeObject(object: Record<string,any>): string | null{
		if(!object){
			return null
		}
		return JSON.stringify(object);
	}

	deserializeObject<T>(object: string):T | null {
		if(!object){
			return null;
		}
		return JSON.parse(object);
	}


	deSerializeAndApplyLookup(serializedLookup: string, targetLookup: Lookup) {
		this.applyLookup(targetLookup, this.deSerializeLookup(serializedLookup));
	}

	private applyLookup(target: Lookup, source: Lookup) {
		Object.keys(source).forEach(key => {
			target[key] = source[key];
		});
	}
}
