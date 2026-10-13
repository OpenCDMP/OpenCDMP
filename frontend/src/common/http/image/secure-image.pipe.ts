import { HttpClient } from '@angular/common/http';
import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { BasePipe } from '@common/base/base.pipe';
import { Observable } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';

@Pipe({ name: 'secureImage' })
export class SecureImagePipe extends BasePipe implements PipeTransform {

	constructor(private http: HttpClient, private sanitizer: DomSanitizer) { super(); }

	transform(url): Observable<SafeUrl> {
		return this.http.get(url, { responseType: 'blob' }).pipe(takeUntil(this._destroyed), map(val => this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(val))));
	}
}
