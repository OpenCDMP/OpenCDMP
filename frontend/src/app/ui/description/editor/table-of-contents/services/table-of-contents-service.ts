import { EventEmitter, Injectable } from "@angular/core";
import { Observable } from "rxjs";

@Injectable()
export class TableOfContentsService {

	private _nextClickedEventEmmiter: EventEmitter<any> = new EventEmitter<any>();
	private _previousClickedEventEmmiter: EventEmitter<any> = new EventEmitter<any>();

	getNextClickedEventObservable(): Observable<any> {
		return this._nextClickedEventEmmiter.asObservable();
	}

	getPreviousEventObservable(): Observable<any> {
		return this._previousClickedEventEmmiter.asObservable();
	}

	nextClicked(): void {
		this._nextClickedEventEmmiter.emit();
	}

	previousClicked(): void {
		this._previousClickedEventEmmiter.emit();
	}
}
