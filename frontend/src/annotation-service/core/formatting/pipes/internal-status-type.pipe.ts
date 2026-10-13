import { Pipe, PipeTransform } from '@angular/core';
import { AnnotationServiceEnumUtils } from '../enum-utils.service';

@Pipe({ name: 'InternalStatusTypeFormat' })
export class InternalStatusTypePipe implements PipeTransform {
	constructor(private enumUtils: AnnotationServiceEnumUtils) { }

	public transform(value): any {
		return this.enumUtils.toInternalStatusString(value);
	}
}
