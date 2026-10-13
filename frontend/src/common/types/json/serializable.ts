export interface Serializable<T> {
	fromJSONObject(item: object): T;
}
