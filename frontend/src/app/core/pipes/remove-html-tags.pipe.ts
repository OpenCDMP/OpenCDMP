import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'removeHtmlTags' })
export class RemoveHtmlTagsPipe implements PipeTransform {
	transform(value: string) {

		const transformedText = value.replace(/(<([^>]+)>)/ig, '');

		return transformedText;
	}
}
