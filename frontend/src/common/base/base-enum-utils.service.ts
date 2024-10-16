import { BaseService } from '@common/base/base.service';

export abstract class BaseEnumUtilsService extends BaseService {

	public getEnumValues<T>(T): Array<T> {

		//getting all numeric values
		const numericValues: T[] = Object.keys(T).map(key => T[key]).filter(value => typeof (value) === 'number');
		if (numericValues.length > 0) { return numericValues; }

		//getting all string values
		const stringValues: T[] = Object.keys(T).map(key => T[key]).filter(value => typeof (value) === 'string');
		if (stringValues.length > 0) { return stringValues; }

		return [];
	}
}
