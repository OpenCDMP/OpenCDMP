import { Injectable } from '@angular/core';
import { BaseEnumUtilsService } from '@common/base/base-enum-utils.service';
import { TranslateService } from '@ngx-translate/core';
import { IsActive } from '../enum/is-active.enum';
import { InternalStatus } from '../enum/internal-status.enum';

@Injectable()
export class AnnotationServiceEnumUtils extends BaseEnumUtilsService {
	constructor(private language: TranslateService) { super(); }


	toIsActiveString(value: IsActive): string {
		switch (value) {
			case IsActive.Active: return this.language.instant('ANNOTATION-SERVICE.TYPES.IS-ACTIVE.ACTIVE');
			case IsActive.Inactive: return this.language.instant('ANNOTATION-SERVICE.TYPES.IS-ACTIVE.INACTIVE');
			default: return '';
		}
	}

	toInternalStatusString(status: InternalStatus): string {
		switch (status) {
			case InternalStatus.Resolved: return this.language.instant('ANNOTATION-SERVICE.TYPES.INTERNAL-STATUS.RESOLVED');
		}
	}

}
