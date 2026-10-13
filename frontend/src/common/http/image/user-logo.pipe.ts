import { Pipe, PipeTransform } from '@angular/core';
import { initials } from '@dicebear/collection';
import { createAvatar } from '@dicebear/core';

export const USER_AVATAR_PLACEHOLDER = 'assets/images/profile-placeholder.png';

@Pipe({ name: 'userLogo', standalone: true })
export class UserLogoPipe implements PipeTransform {

	transform(username: string): string {
		if (!username) {
			return USER_AVATAR_PLACEHOLDER;
		}

		return createAvatar(initials, {
			seed: username,
			fontSize: 45,
		}).toDataUri();
	}
}
