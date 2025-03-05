import { Pipe, PipeTransform } from '@angular/core';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';

@Pipe({
    name: 'ReferenceSourceTypeFormat',
    standalone: false
})
export class ReferenceSourceTypePipe implements PipeTransform {
	constructor(private enumUtils: EnumUtils) { }

	public transform(value): any {
		return this.enumUtils.toReferenceSourceTypeString(value);
	}
}
