import { Pipe, PipeTransform } from '@angular/core';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';

@Pipe({ name: 'IsActiveTypeFormat' })
export class IsActiveTypePipe implements PipeTransform {
	constructor(private enumUtils: NotificationServiceEnumUtils) { }

	public transform(value): any {
		return this.enumUtils.toIsActiveString(value);
	}
}
