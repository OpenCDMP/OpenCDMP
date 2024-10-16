import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable()
export class ProgressIndicationService {
	public progressIndicator: BehaviorSubject<boolean>;

	private requestsRunning = 0;

	constructor() {
		this.progressIndicator = new BehaviorSubject<boolean>(false);
	}

	public getProgressIndicationObservable(): Observable<boolean> {
		return this.progressIndicator.asObservable();
	}

	public list(): number {
		return this.requestsRunning;
	}

	public show(): void {
		this.requestsRunning++;
		if (this.requestsRunning === 1) {
			this.progressIndicator.next(true);
		}
	}

	public dismiss(): void {
		if (this.requestsRunning > 0) {
			this.requestsRunning--;
			if (this.requestsRunning === 0) {
				this.progressIndicator.next(false);
			}
		}
	}
}
