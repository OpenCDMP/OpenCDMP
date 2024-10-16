
import { Component } from '@angular/core';
import { BaseComponent } from '@common/base/base.component';


@Component({
	selector: 'app-tenant-configuration-editor-component',
	templateUrl: 'tenant-configuration-editor.component.html',
	styleUrls: ['./tenant-configuration-editor.component.scss'],
})
export class TenantConfigurationEditorComponent extends BaseComponent {

	constructor(
	) {
		super();
	}

	ngOnInit() {
	}

}
