import { Pipe, PipeTransform } from '@angular/core';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';

@Pipe({ name: 'UsageLimitTargetMetricFormat' })
export class UsageLimitTargetMetricPipe implements PipeTransform {
	constructor(private enumUtils: EnumUtils) { }

	public transform(value): any {
		return this.enumUtils.toUsageLimitTargetMetricString(value);
	}
}
