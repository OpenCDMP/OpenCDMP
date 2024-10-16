import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
	name: 'lowercaseFirstLetter'
})
export class LowercaseFirstLetterPipe implements PipeTransform {


	transform(value: string): string {

		if (!value) {
			return value;
		}


		const toReturn = value.charAt(0).toLowerCase() + value.slice(1);
		return toReturn;
	}
}