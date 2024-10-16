export interface QueryResult<T> {
	count: number;
	items: T[];
}