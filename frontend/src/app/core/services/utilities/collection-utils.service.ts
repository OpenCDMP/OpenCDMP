import { Injectable } from '@angular/core';

@Injectable()
export class CollectionUtils {
	public static defaultArrayComparer<ItemType>(itemComparer: EqualityComparer<ItemType> = new StrictEqualityComparer()): EqualityComparer<ItemType[]> {
		return new InOrderComparer(itemComparer);
	}

	public static defaultIterableComparer<ItemType>(itemComparer: EqualityComparer<ItemType> = new StrictEqualityComparer()): EqualityComparer<Iterable<ItemType>> {
		return new InOrderComparer(itemComparer);
	}

	public static asArray<ItemType>(collection: Iterable<ItemType>): ItemType[] {
		return asArray(collection);
	}

	public equal<ItemType>(left: ItemType[], right: ItemType[], arrayComparer?: EqualityComparer<ItemType[]>): boolean {
		return equal(left, right, arrayComparer);
	}

	public setEqual<ItemType>(left: ItemType[], right: ItemType[]): boolean {
		return equal(left, right, new SetComparer());
	}

	public any<ItemType>(collection: Iterable<ItemType>, predicate?: (item: ItemType, index: number) => boolean): boolean {
		return any(collection, predicate);
	}

	public all<ItemType>(collection: Iterable<ItemType>, predicate: (item: ItemType, index: number) => boolean): boolean {
		return all(collection, predicate);
	}

	public flatten<ItemType>(source: ItemType[][]): ItemType[] {
		return source.reduce(flatteningReducer, []);
	}
}

export interface EqualityComparer<ItemType> {
	equal(left: ItemType, right: ItemType);
}

function equal<ItemType>(left: ItemType[], right: ItemType[], arrayComparer?: EqualityComparer<ItemType[]>): boolean {
	arrayComparer = arrayComparer || new InOrderComparer();
	return arrayComparer.equal(left, right);
}

function any<ItemType>(collection: Iterable<ItemType>, predicate?: (item: ItemType, index: number) => boolean): boolean {
	if (!collection) { return false; }
	const array = asArray(collection);
	if (!predicate) { predicate = () => array.length > 0; }
	return array.some(predicate);
}

function all<ItemType>(collection: Iterable<ItemType>, predicate: (item: ItemType, index: number) => boolean): boolean {
	if (!collection) { return false; }
	const array = asArray(collection);
	let index = 0, result = true;
	while (result && index < array.length) {
		result = predicate(array[index], index);
		index++;
	}
	return result;
}

function asArray<ItemType>(collection: Iterable<ItemType>): ItemType[] {
	return Array.isArray(collection) ? collection as ItemType[] : Array.from(collection);
}

function flatteningReducer<ItemType>(target: ItemType[], currentValue: ItemType[]): ItemType[] {
	return [...target, ...currentValue];
}

export class StrictEqualityComparer<ItemType> implements EqualityComparer<ItemType> {
	equal(left: ItemType, right: ItemType) {
		return left === right;
	}
}

export class InOrderComparer<IterableType extends Iterable<ItemType>, ItemType> implements EqualityComparer<IterableType> {
	constructor(private itemComparer: EqualityComparer<ItemType> = new StrictEqualityComparer()) { }
	equal(left: IterableType, right: IterableType) {
		if (!left) { return !right; }
		if (!right) { return false; }
		const leftArray = asArray(left), rightArray = asArray(right);
		return leftArray.length === leftArray.length && all(leftArray, (x, index) => this.itemComparer.equal(x, rightArray[index]));
	}
}

export class SetComparer<IterableType extends Iterable<ItemType>, ItemType> implements EqualityComparer<IterableType> {
	equal(left: IterableType, right: IterableType) {
		if (!left) { return !right; }
		if (!right) { return false; }
		const leftSet = new Set(left), rightSet = new Set(right);
		return leftSet.size === rightSet.size && all(leftSet, x => rightSet.has(x));
	}
}
