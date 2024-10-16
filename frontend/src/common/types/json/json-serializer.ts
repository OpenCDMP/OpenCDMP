import { Serializable } from '@common/types/json/serializable';

export class JsonSerializer<T extends Serializable<T>> {
	constructor(private constructorOfT: { new(): T }) {
	}

	public fromJSONArray(items: any[]): T[] {
		return items.map(x => this.fromJSONObject(x));
	}

	public fromJSONObject(item: any): T {
		return new this.constructorOfT().fromJSONObject(item);
	}
}
