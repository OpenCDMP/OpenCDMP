import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

@Component({
    templateUrl: './page-not-found.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [TranslateModule],
})
export class PageNotFoundComponent implements OnInit {

	constructor() { }

	ngOnInit() {
	}

}
