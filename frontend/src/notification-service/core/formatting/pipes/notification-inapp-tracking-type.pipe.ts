import { Pipe, PipeTransform } from '@angular/core';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';

@Pipe({
    name: 'NotificationInAppTrackingTypeFormat',
    standalone: false
})
export class NotificationInAppTrackingTypePipe implements PipeTransform {
	constructor(private enumUtils: NotificationServiceEnumUtils) { }

	public transform(value): any {
		return this.enumUtils.toNotificationInAppTrackingString(value);
	}
}
