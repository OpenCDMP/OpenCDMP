import { Injectable, Injector } from '@angular/core';

@Injectable()
export class PipeService {
	constructor(private injector: Injector) { }

	public getPipe<T>(T): T {
		return this.injector.get(T);
	}
}
