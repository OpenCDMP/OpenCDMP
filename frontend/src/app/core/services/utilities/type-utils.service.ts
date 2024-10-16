import { Injectable } from '@angular/core';

@Injectable()
export class TypeUtils {
	public isString(value: any): value is string { return typeof value === 'string'; }
}
