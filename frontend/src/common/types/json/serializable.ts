export interface Serializable<T> {
	fromJSONObject(item: Object): T;
}
