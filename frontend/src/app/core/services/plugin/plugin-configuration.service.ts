import { computed, Injectable, Signal } from '@angular/core';
import { BaseService } from '@common/base/base.service';
import { AuthService } from '../auth/auth.service';
import { FileTransformerService } from '../file-transformer/file-transformer.service';
import { PluginConfiguration, PluginConfigurationField, PluginConfigurationUser, PluginConfigurationUserField, PluginRepositoryConfiguration, PluginRepositoryUserConfiguration } from '@app/core/model/plugin-configuration/plugin-configuration';
import { EvaluatorService } from '../evaluator/evaluator.service';
import { PluginType } from '@app/core/common/enum/plugin-type';
import { PluginEntityType } from '@app/core/common/enum/plugin-entity-type';
import { DepositService } from '../deposit/deposit.service';

@Injectable()
export class PluginConfigurationService extends BaseService {

	constructor(
		private fileTransformerService: FileTransformerService,
		private evaluatorService: EvaluatorService,
		private depositService: DepositService,
		private authentication: AuthService,
	) { 
        super();
    }

	//
	// plugin configuration 
    //

	// repositories
	pluginRepositoryConfiguration: Signal<PluginRepositoryConfiguration[]> = computed(() => {
        const authenticated = this.authentication.currentAccountIsAuthenticated();
		const fileTranformerRepositories = this.fileTransformerService.availableFormats();
        const evaluators = this.evaluatorService.availableEvaluators();
        const deposits = this.depositService.availableDeposits();
        
        if(!authenticated || !fileTranformerRepositories || !evaluators || !deposits) { return; }
        let res = [];
		fileTranformerRepositories?.forEach(x => {
			if (x.fileTransformerId && x.configurationFields?.length > 0) {
				res.push( {
					repositoryId: x.fileTransformerId,
					pluginType: x.pluginType ?? PluginType.FileTransformer,
					fields: x.configurationFields
				})
			}
		})

		evaluators?.forEach(x => {
			if (x.evaluatorId && x.configurationFields?.length > 0) {
				res.push( {
					repositoryId: x.evaluatorId,
					pluginType: x.pluginType ?? PluginType.Evaluation,
					fields: x.configurationFields
				})
			}
		})

		deposits?.forEach(x => {
			if (x.repositoryId && x.configurationFields?.length > 0) {
				res.push( {
					repositoryId: x.repositoryId,
					pluginType: x.pluginType ?? PluginType.Deposit,
					fields: x.configurationFields
				})
			}
		})
        return res;
	}) 

	public getPluginRepositoryConfigurationFor(pluginType: PluginType, entityTypes: PluginEntityType[]) {
		let pluginRepositoryConfiguration = JSON.parse(JSON.stringify(this.pluginRepositoryConfiguration()));
		if (this.pluginRepositoryConfiguration() && pluginType) pluginRepositoryConfiguration = pluginRepositoryConfiguration.filter(x => x.pluginType == pluginType);
		if (this.pluginRepositoryConfiguration() && entityTypes?.length > 0) {
			pluginRepositoryConfiguration.forEach(plugin => {
				plugin.fields = plugin.fields?.filter(x => x.appliesTo.some(x => entityTypes.includes(x)))
			})
		}
		return pluginRepositoryConfiguration;
	}

	public availablePlugins: Signal<PluginConfiguration[]> = computed(() => {
        if(!this.pluginRepositoryConfiguration()){
            return;
        }
		const plugins = [];
		this.pluginRepositoryConfiguration().forEach( repo => {
			if (repo.fields?.length > 0) {
				let pluginFields: PluginConfigurationField[] = [];
				repo?.fields?.forEach(configField => {
					pluginFields.push({
						code: configField.code,
						fileValue: null,
						textValue: null
					})
				})
				plugins.push({
					fields: pluginFields,
					pluginCode: repo.repositoryId,
					pluginType: repo.pluginType
				})
				}
			}
		)
        return this.removeDuplicates(plugins);
    });


	public getAvailablePluginsFor(pluginType: PluginType, entityTypes: PluginEntityType[]) {
        let availablePlugins = [];
		if (this.availablePlugins() && pluginType && !entityTypes){
            availablePlugins = this.availablePlugins().filter(x => x.pluginType == pluginType)
        }else if (this.availablePlugins() && entityTypes) {
			const pluginRepositoriesConfiguration = this.getPluginRepositoryConfigurationFor(pluginType, entityTypes);
			pluginRepositoriesConfiguration?.forEach( repo => {
				if (repo.fields?.length > 0) {
					let pluginFields: PluginConfigurationField[] = [];
					repo?.fields?.forEach(configField => {
						pluginFields.push({
							code: configField.code,
							fileValue: null,
							textValue: null
						})
					})
					availablePlugins.push({
						fields: pluginFields,
						pluginCode: repo.repositoryId,
						pluginType: repo.pluginType
					})
					}
				}
			)
		}

		availablePlugins = this.removeDuplicates(availablePlugins);

		return availablePlugins;
	}

	//
	// user plugin configuration 
    //

	pluginRepositoryUserConfiguration: Signal<PluginRepositoryUserConfiguration[]> = computed(() => {
        const authenticated = this.authentication.currentAccountIsAuthenticated();
		const fileTranformerRepositories = this.fileTransformerService.availableFormats();
        const evaluators = this.evaluatorService.availableEvaluators();
        const deposits = this.depositService.availableDeposits();
        
        if(!authenticated || !fileTranformerRepositories || !evaluators || !deposits) { return; }
        let res = [];
		fileTranformerRepositories?.forEach(x => {
			if (x.fileTransformerId && x.userConfigurationFields?.length > 0) {
				res.push( {
					repositoryId: x.fileTransformerId,
					pluginType: x.pluginType ?? PluginType.FileTransformer,
					fields: x.userConfigurationFields
				})
			}
		})

		evaluators?.forEach(x => {
			if (x.evaluatorId && x.userConfigurationFields?.length > 0) {
				res.push( {
					repositoryId: x.evaluatorId,
					pluginType: x.pluginType ?? PluginType.Evaluation,
					fields: x.userConfigurationFields
				})
			}
		})

		deposits?.forEach(x => {
			if (x.repositoryId && x.userConfigurationFields?.length > 0) {
				res.push( {
					repositoryId: x.repositoryId,
					pluginType: x.pluginType ?? PluginType.Deposit,
					fields: x.userConfigurationFields
				})
			}
		})
        return res;
	})

	public availableUserPlugins: Signal<PluginConfigurationUser[]> = computed(() => {
        if(!this.pluginRepositoryUserConfiguration()){
            return;
        }
		const plugins: PluginConfigurationUser[] = [];
		this.pluginRepositoryUserConfiguration().forEach( repo => {
			if (repo.fields?.length > 0) {
				let pluginFields: PluginConfigurationUserField[] = [];
				repo?.fields?.forEach(configField => {
					pluginFields.push({
						code: configField.code,
						fileValue: null,
						textValue: null
					})
				})
				plugins.push({
					userFields: pluginFields,
					pluginCode: repo.repositoryId,
					pluginType: repo.pluginType
				})
				}
			}
		)
        return this.removeUserFieldsDuplicates(plugins);
    });

	//
	//
	//

	private removeDuplicates(availablePlugins: PluginConfiguration[]) {
		return availablePlugins?.filter((value, index, self) =>
			index === self.findIndex((x) => (
			  x.pluginCode === value.pluginCode
			))
		) ?? [];
	}

	private removeUserFieldsDuplicates(availablePlugins: PluginConfigurationUser[]) {
		return availablePlugins?.filter((value, index, self) =>
			index === self.findIndex((x) => (
			  x.pluginCode === value.pluginCode
			))
		) ?? [];
	}


}
