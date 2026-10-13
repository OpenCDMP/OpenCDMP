import { Directive } from '@angular/core';
import { USER_AVATAR_PLACEHOLDER } from '@common/http/image/user-logo.pipe';

@Directive({
	selector: 'img[appFallbackAvatar]',
	standalone: true,
	host: {
		'(error)': 'onError($event)',
	},
})
export class FallbackAvatarDirective {

	onError(event: Event): void {
		(event.target as HTMLImageElement).src = USER_AVATAR_PLACEHOLDER;
	}
}
