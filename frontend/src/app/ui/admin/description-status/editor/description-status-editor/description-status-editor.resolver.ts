import { Injectable } from "@angular/core";
import { ActivatedRouteSnapshot, RouterStateSnapshot } from "@angular/router";
import { DescriptionStatus, DescriptionStatusDefinition, DescriptionStatusDefinitionAuthorization, DescriptionStatusDefinitionAuthorizationItem } from "@app/core/model/description-status/description-status";
import { StorageFile } from "@app/core/model/storage-file/storage-file";
import { DescriptionStatusService } from "@app/core/services/description-status/description-status.service";
import { BreadcrumbService } from "@app/ui/misc/breadcrumb/breadcrumb.service";
import { BaseEditorResolver } from "@common/base/base-editor.resolver";
import { Guid } from "@common/types/guid";
import { takeUntil, tap } from "rxjs";
import { nameof } from "ts-simple-nameof";

@Injectable()
export class DescriptionStatusEditorResolver extends BaseEditorResolver{

    constructor(private descriptionStatusService: DescriptionStatusService, private breadcrumbService: BreadcrumbService) {
		super();
	}

    public static lookupFields(): string[] {
		return [
            nameof<DescriptionStatus>(x => x.id),
            nameof<DescriptionStatus>(x => x.name),
            nameof<DescriptionStatus>(x => x.description),
            nameof<DescriptionStatus>(x => x.action),
            nameof<DescriptionStatus>(x => x.ordinal),
            nameof<DescriptionStatus>(x => x.internalStatus),
            nameof<DescriptionStatus>(x => x.definition),
            [nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.authorization)].join('.'),
            [nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.authorization), nameof<DescriptionStatusDefinitionAuthorization>(x => x.edit), nameof<DescriptionStatusDefinitionAuthorizationItem>(x => x.roles)].join('.'),
            [nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.authorization), nameof<DescriptionStatusDefinitionAuthorization>(x => x.edit), nameof<DescriptionStatusDefinitionAuthorizationItem>(x => x.planRoles)].join('.'),
            [nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.authorization), nameof<DescriptionStatusDefinitionAuthorization>(x => x.edit), nameof<DescriptionStatusDefinitionAuthorizationItem>(x => x.allowAnonymous)].join('.'),
            [nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.authorization), nameof<DescriptionStatusDefinitionAuthorization>(x => x.edit), nameof<DescriptionStatusDefinitionAuthorizationItem>(x => x.allowAuthenticated)].join('.'),

            [nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.availableActions)].join('.'),
            [nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.matIconName)].join('.'),
            [nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.storageFile), nameof<StorageFile>(x => x.id), ].join('.'), 
            [nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.statusColor)].join('.'),


            nameof<DescriptionStatus>(x => x.updatedAt),
            nameof<DescriptionStatus>(x => x.createdAt),
            nameof<DescriptionStatus>(x => x.hash),
            nameof<DescriptionStatus>(x => x.isActive),
            nameof<DescriptionStatus>(x => x.belongsToCurrentTenant),
        ]
    }

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        const fields = [
			...DescriptionStatusEditorResolver.lookupFields()
		];
		const id = route.paramMap.get('id');
		if (id != null) {
			return this.descriptionStatusService.getSingle(Guid.parse(id), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.name)), takeUntil(this._destroyed));
		}
    }

}