import { Pipe, PipeTransform } from '@angular/core';
import { SafeUrl } from '@angular/platform-browser';
import { User } from '@app/core/model/user/user';
import { BasePipe } from '@common/base/base.pipe';
import { initials } from '@dicebear/collection';
import { createAvatar } from '@dicebear/core';
import { from, Observable, of } from 'rxjs';

@Pipe({ name: 'userLogoAsync' , standalone: true})
export class UserLogoPipe extends BasePipe implements PipeTransform {


	private readonly userPlaceholderImage = 'assets/images/profile-placeholder.png';
	constructor() { super(); }

	transform(user: User): Observable<SafeUrl> {
        if(!user){
            return of(this.userPlaceholderImage);
        }
        return this._getFallbackImageForUser(user);
	}

    private _getFallbackImageForUser(user: User): Observable<string>{
		const code = user?.name;
		if(!code){ 
			return of(this.userPlaceholderImage);
		}

		return from(
			createAvatar(initials, {
				seed: code,
				fontSize:45,
				
			}).toDataUri()
		)
	}
}
