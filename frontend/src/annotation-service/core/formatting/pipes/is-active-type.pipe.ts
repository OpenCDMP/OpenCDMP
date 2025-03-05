import { Pipe, PipeTransform } from '@angular/core';
import { AnnotationServiceEnumUtils } from '../enum-utils.service';

@Pipe({
    name: 'IsActiveTypeFormat',
    standalone: false
})
export class IsActiveTypePipe implements PipeTransform {
	constructor(private enumUtils: AnnotationServiceEnumUtils) { }

	public transform(value): any {
		return this.enumUtils.toIsActiveString(value);
	}
}
