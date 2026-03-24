
import { Component } from '@angular/core';
import { PluginConfigurationService } from '@app/core/services/plugin/plugin-configuration.service';
import { BaseComponent } from '@common/base/base.component';


@Component({
    selector: 'app-tenant-configuration-editor-component',
    templateUrl: 'tenant-configuration-editor.component.html',
    styleUrls: ['./tenant-configuration-editor.component.scss'],
    standalone: false
})
export class TenantConfigurationEditorComponent extends BaseComponent {

	constructor(
		private pluginConfigurationService: PluginConfigurationService
	) {
		super();
	}

	ngOnInit() {
	}

	get availablePlugins() {
		return this.pluginConfigurationService.availablePlugins();
	}

}
