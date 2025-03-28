import { Pipe, PipeTransform } from '@angular/core';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';

@Pipe({
    name: 'SupportiveMaterialFieldTypeFormat',
    standalone: false
})
export class SupportiveMaterialFieldTypePipe implements PipeTransform {
	constructor(private enumUtils: EnumUtils) { }

	public transform(value): any {
		return this.enumUtils.toSupportiveMaterialTypeString(value);
	}
}
