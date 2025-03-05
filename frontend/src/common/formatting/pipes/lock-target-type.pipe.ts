import { Pipe, PipeTransform } from '@angular/core';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';

@Pipe({
    name: 'LockTargetTypeFormat',
    standalone: false
})
export class LockTargetTypePipe implements PipeTransform {
	constructor(private enumUtils: EnumUtils) { }

	public transform(value): any {
		return this.enumUtils.toLockTargetTypeString(value);
	}
}