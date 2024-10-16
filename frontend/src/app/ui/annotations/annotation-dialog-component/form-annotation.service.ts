import { Injectable } from '@angular/core';
import { Annotation } from '@annotation-service/core/model/annotation.model';
import { AnnotationLookup } from '@annotation-service/core/query/annotation.lookup';
import { AnnotationService } from '@annotation-service/services/http/annotation.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { BaseService } from '@common/base/base.service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';


@Injectable({
	providedIn: 'any',
})
export class FormAnnotationService extends BaseService {

	private entityId: Guid;
	private entityType: string;
	private annotationsPerAnchor: Map<string, number>;
	private annotationCountSubject: BehaviorSubject<Map<string, number>> = new BehaviorSubject<Map<string, number>>(null);
	private openAnnotationSubject: Subject<any> = new Subject<any>();

	constructor(
		private annotationService: AnnotationService,
		private uiNotificationService: UiNotificationService,
		private language: TranslateService
	) {
		super();
	}

	init(entityId: Guid, entityType: string) {
		this.entityId = entityId;
		this.entityType = entityType;
		this.refreshAnnotations();
	}

	public getAnnotationCountObservable(): Observable<Map<string, number>> {
		return this.annotationCountSubject.asObservable();
	}

	public getOpenAnnotationSubjectObservable(): Observable<string> {
		return this.openAnnotationSubject.asObservable();
	}

	public οpenAnnotationDialog(next: any): void {
		this.openAnnotationSubject.next(next);
	}

	public refreshAnnotations() {
		const lookup: AnnotationLookup = new AnnotationLookup();
		lookup.entityIds = [this.entityId];
		lookup.entityTypes = [this.entityType];
		lookup.project = {
			fields: [
				nameof<Annotation>(x => x.id),
				nameof<Annotation>(x => x.anchor),
			]
		};

		this.annotationService.query(lookup)
			.pipe(takeUntil(this._destroyed))
			.subscribe(
				data => {
					this.annotationsPerAnchor = new Map();
					for (const item of data.items) {
						if (!this.annotationsPerAnchor.has(item.anchor)) {
							this.annotationsPerAnchor.set(item.anchor, 0);
						}
						this.annotationsPerAnchor.set(item.anchor, this.annotationsPerAnchor.get(item.anchor) + 1);
					}
					this.annotationCountSubject.next(this.annotationsPerAnchor);
				},
				error => this.onCallbackError(error),
			);
	}

	private onCallbackError(error: any) {
		this.uiNotificationService.snackBarNotification(this.language.instant(error.message), SnackBarNotificationLevel.Error);
	}
}

