import { AsyncPipe, CommonModule } from '@angular/common';
import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PlanBlueprintFieldCategory } from '@app/core/common/enum/plan-blueprint-field-category';
import { WSActionType } from '@app/core/common/enum/ws-action-type';
import { ExtraFieldInSection, PlanBlueprintDefinitionSection, ReferenceTypeFieldInSection, SystemFieldInSection, UploadFieldInSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { Plan } from '@app/core/model/plan/plan';
import { User } from '@app/core/model/user/user';
import { UserActionPayload, WSMessage } from '@app/core/model/websocket/ws-message.model';
import { AuthService } from '@app/core/services/auth/auth.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { PlanWebSocketService } from '@app/core/services/websocket/plan-websocket.service';
import { BaseComponent } from '@common/base/base.component';
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { UserLogoPipe } from '@common/http/image/user-logo.pipe';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { TranslateModule } from '@ngx-translate/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import { Observable, takeUntil } from 'rxjs';
import { DescriptionInfo } from '../plan-editor-blueprint/plan-temp-storage.service';

@Component({
    selector: 'app-plan-active-users',
    imports: [CommonModule, TranslateModule, AsyncPipe, UserLogoPipe, CommonFormattingModule, MatTooltipModule],
    templateUrl: './plan-active-users.component.html',
    styleUrl: './plan-active-users.component.scss'
})
export class PlanActiveUsersComponent extends BaseComponent {
    @Input() item: Plan;
    @Input() skipNavigation: boolean = false; 
    @Input() registerSelf: boolean = true; 
    @Output() onNavigate = new EventEmitter<UserActionPayload>;


    private _descriptionInfoMap: Map<string, DescriptionInfo>;
    
    //Only do this instead of recalculating the user position in every angular cycle 
    // @Input() set descriptionInfoMap(val: Map<string, DescriptionInfo>){
    //     this.visibilityRuleChangeObserver?.unsubscribe();
    //     this.visibilityRuleChangeObserver = null;
    //     this._descriptionInfoMap = val;
    //     if(val?.size){
    //         let visibilityRuleServices: VisibilityRulesService[] = [];
    //         this._descriptionInfoMap.forEach((value, key) => {
    //             if(value.visibilityRulesService){
    //                 visibilityRuleServices.push(value.visibilityRulesService)
    //             }
    //         })
    //         if(visibilityRuleServices?.length){
    //             this.visibilityRuleChangeObserver = 
    //                 forkJoin(visibilityRuleServices.map(x => x.rulesChangedSubject))
    //                 .pipe(takeUntil(this._destroyed))
    //                 .subscribe(() => {
    //                     if(this.activeUsersMap?.size){
    //                         this.refreshActiveUserPositions()
    //                     }
    //                 })

    //         }
    //     }
    // }

    get descriptionInfoMap(){
        return this._descriptionInfoMap;
    }

    constructor(
        private planService: PlanService,
        private planWebSocketService: PlanWebSocketService,
        private httpErrorHandlingService: HttpErrorHandlingService,
        private authService: AuthService,
        private enumUtils: EnumUtils
    ) {
        super();
    }

    activeUsersMap: Map<Guid, ActiveUserInfo> = new Map([]);
    stompSubscription: StompSubscription;
    stompClient$: Observable<Client>;
    
    activeUserArray = (map : Map<Guid, ActiveUserInfo>) => {
        return Array.from(map?.values() ?? []);
    }
    
    ngOnInit() {
        if (this.item?.id && this.item.isActive === IsActive.Active) {
            this.planService.getActiveUsers(this.item?.id)
                .pipe(takeUntil(this._destroyed))
                .subscribe(
                    (result) => {
                        let validUsers = new Set(result?.filter(x => this.isValidPlanUser(x.id))?.map(x => x.id) ?? []); //Remove duplicates
                        validUsers.forEach((userId) => {
                            let user = result.find(x => x.id === userId);
                            if(user){
                                this.activeUsersMap.set(userId, {user, payload: null});
                            }
                        })

                        this.stompClient$ = this.planWebSocketService.getClient$().pipe(takeUntil(this._destroyed));

                        this.stompClient$.subscribe(stompClient => {
                            if (stompClient?.connected) {

                                this.stompSubscription = stompClient.subscribe(`/topic/plan/${this.item?.id?.toString()}/users`, (message) => {
                                    try {
                                        const response = JSON.parse(message.body) as WSMessage;
                                        if(!this.isValidPlanUser(response?.producer?.id)){
                                            return;
                                        }
                                        if (response?.actionType == WSActionType.PlanJoin) {
                                            // add user
                                            let user = response.producer as User;
                                            this.activeUsersMap.set(user.id, {user, payload: null});
                                        } else if (response?.actionType == WSActionType.PlanLeave) {
                                            // remove user
                                            this.activeUsersMap.delete(response.producer?.id);
                                        } else if (response?.actionType == WSActionType.PlanUserAction) {
                                            if(this.activeUsersMap.has(response?.producer?.id)){
                                                this.activeUsersMap.set(response.producer.id, {
                                                    user: response.producer,
                                                    payload: response.payload
                                                })
                                            }
                                        }

                                    } catch (err) {
                                        console.error(err);
                                    }
                                });

                                if(this.registerSelf){
                                    stompClient.publish({
                                        destination: `/app/plan/${this.item?.id?.toString()}/join`,
                                        body: ''
                                    });
                                }

                                stompClient.subscribe("/user/queue/errors", function (message) {
                                    console.log("Error " + message.body);
                                });

                            }

                        });
                    },
                    (error) => {
                        this.httpErrorHandlingService.handleBackedRequestError(error)
                    }
                );
        }
    }

    private planSections(): PlanBlueprintDefinitionSection[] {
        return this.item?.blueprint?.definition?.sections
    }

    private parsePosition(payload: UserActionPayload): ParsedPosition {
        let descriptionPosition = '';
        let planPosition = '';
        if(payload?.blueprintSectionId){
            let sections = this.planSections();

            let section = sections?.find(x => x.id == payload.blueprintSectionId);
            let step = section?.ordinal ?? null;

            if(section) {
                let field;
                if(payload.blueprintFieldId){
                    field = section?.fields?.find(x => x.id == payload.blueprintFieldId) ?? null;
                }
                planPosition = field ? `${step}.${field.ordinal}. ${this.getFieldLabel(field)}` : `${step}. ${section.label}`
            }

        }
        if(payload?.descriptionId){
            let keyId = payload.descriptionFieldSetId ?? payload.descriptionSectionId ?? payload.descriptionPageId ?? payload.descriptionId;
            let element = document.getElementById(keyId + '-label');
            descriptionPosition = element?.textContent ?? null;
        }
        return {
            descriptionPosition,
            planPosition
        }
    }

    private getFieldLabel(field: SystemFieldInSection | ExtraFieldInSection | ReferenceTypeFieldInSection | UploadFieldInSection): string {
        if(!field.label){
            switch(field.category){
                case(PlanBlueprintFieldCategory.System):
                    return this.enumUtils.toPlanBlueprintSystemFieldTypeString((field as SystemFieldInSection).systemFieldType)
                case(PlanBlueprintFieldCategory.ReferenceType):
                    return (field as ReferenceTypeFieldInSection).referenceType.name
            }
        } else {
            return field.label;
        }
    }

    private isValidPlanUser(userId: Guid) {
        return this.item?.planUsers?.find(x => x.user?.id === userId) && userId != this.authService.userId();
    }

    getTooltipInfo(user: ActiveUserInfo): string {
        let position = this.parsePosition(user?.payload);
        return `${user.user.name} \n\n` +
        (position?.descriptionPosition?.length ? position.descriptionPosition : (position?.planPosition ?? '')) + ' \n'
    }

    ngOnDestroy(): void {
        super.ngOnDestroy();
        this.destroyWebsocket();
    }

    destroyWebsocket(){
        if (this.item?.id) {
            this.stompClient$.subscribe(stompClient => {
                if (stompClient && stompClient.connected && this.stompSubscription) {
                    this.stompSubscription.unsubscribe();
                    if(this.registerSelf){
                        stompClient.publish({
                            destination: `/app/plan/${this.item.id}/leave`,
                            body: ''
                        });
                    }
                }
            })
        }
    }

    protected navigateToPosition(userInfo: ActiveUserInfo){
        this.onNavigate.emit(userInfo.payload);
    }
}

interface ParsedPosition {
    planPosition: string;
    descriptionPosition: string;
}

interface ActiveUserInfo {
    user: User;
    payload: UserActionPayload
}