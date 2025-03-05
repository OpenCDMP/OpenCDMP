import { Component, computed, effect, input, output } from '@angular/core';
import { PlanAccessType } from '@app/core/common/enum/plan-access-type';
import { PlanBlueprintFieldCategory } from '@app/core/common/enum/plan-blueprint-field-category';
import { PlanBlueprintExtraFieldDataType } from '@app/core/common/enum/plan-blueprint-field-type';
import { PlanBlueprintSystemFieldType } from '@app/core/common/enum/plan-blueprint-system-field-type';
import { LanguageInfo } from '@app/core/model/language-info';
import { DragAndDropAccessibilityService } from '@app/core/services/accessibility/drag-and-drop-accessibility.service';
import { LanguageInfoService } from '@app/core/services/culture/language-info-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { PlanEditorForm, PlanEditorModel } from '../../plan-editor.model';
import { FormArray, FormControl, FormGroup, UntypedFormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { PlanContactPrefillDialogComponent } from '@app/ui/plan/plan-contact-prefill-dialog/plan-contact-prefill-dialog.component';
import { ExtraFieldInSection, FieldInSection, FieldInSectionPersist, PlanBlueprintDefinitionSection, ReferenceTypeFieldInSection, SystemFieldInSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { takeUntil } from 'rxjs';
import { BaseComponent } from '@common/base/base.component';
import { moveItemInArray } from '@angular/cdk/drag-drop';
import { PlanAssociatedUser } from '@app/core/model/user/user';
import { TranslateService } from '@ngx-translate/core';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { Guid } from '@common/types/guid';

@Component({
  selector: 'app-plan-internal-blueprint-field-editor',
  templateUrl: './plan-internal-blueprint-field-editor.component.html',
  styleUrl: './plan-internal-blueprint-field-editor.component.scss',
  standalone: false,
  providers: [DragAndDropAccessibilityService]
})
export class PlanInternalBlueprintFieldEditorComponent extends BaseComponent{
    field = input.required<FieldInSection | FieldInSectionPersist>();

    fieldPath = input<string>();
    formGroup = input<FormGroup<PlanEditorForm>>();
    dependencies = computed(() => this.formGroup()?.get('properties')?.get('planBlueprintValues'))
    canEdit = input<boolean>(true);
    isNew = input<boolean>(false);
    isActivePlan = input<boolean>(true);
    editorModel = input<PlanEditorModel>();
    
    referenceType = input<ReferenceType>();
    sections = input<PlanBlueprintDefinitionSection[]>(null);
    annotationsPerAnchor =  input<Map<string, number>>();
    
    reApplyPropertiesValidators = output();
    showAnnotations = output<Guid>();

    planBlueprintSectionFieldCategoryEnum = PlanBlueprintFieldCategory;
    planBlueprintSystemFieldTypeEnum = PlanBlueprintSystemFieldType;
    planBlueprintExtraFieldDataTypeEnum = PlanBlueprintExtraFieldDataType;
    planAccessTypeEnum = PlanAccessType;
    planAccessTypeEnumValues = this.enumUtils.getEnumValues<PlanAccessType>(PlanAccessType);

    
    getLanguageInfos(): LanguageInfo[] {
        return this.languageInfoService.getLanguageInfoValues();
    }

    reorderAssistiveText = computed(() => this.dragAndDropService.assistiveTextSignal());
    get reorderMode(){
        return !this.formGroup().disabled && this.canEdit() && this.dragAndDropService.reorderMode;
    }

    get planBlueprintValueControl(){
        return this.formGroup()?.get('properties')?.get('planBlueprintValues')?.get(this.field().id.toString())
    }

    constructor(
        public enumUtils: EnumUtils,
        protected language: TranslateService,
        private uiNotificationService: UiNotificationService,
        private languageInfoService: LanguageInfoService,
        private dragAndDropService: DragAndDropAccessibilityService,
        private dialog: MatDialog,
        private routerUtils: RouterUtilsService
    ){
        super();
    }

    copyLink(fieldId: string): void {
        const el = document.createElement('textarea');
        let domain = `${window.location.protocol}//${window.location.hostname}`;
        if (window.location.port && window.location.port != '') domain += `:${window.location.port}`
        const descriptionSectionPath = this.routerUtils.generateUrl(['plans/edit', this.editorModel()?.id?.toString(), 'f', fieldId].join('/'));
        el.value = domain + descriptionSectionPath;
        el.setAttribute('readonly', '');
        el.style.position = 'absolute';
        el.style.left = '-9999px';
        document.body.appendChild(el);
        el.select();
        document.execCommand('copy');
        document.body.removeChild(el);
        this.uiNotificationService.snackBarNotification(
            this.language.instant('DESCRIPTION-EDITOR.QUESTION.EXTENDED-DESCRIPTION.COPY-LINK-SUCCESSFUL'), 
            SnackBarNotificationLevel.Success
        );
    }

    //
    //
    // Contacts
    //
    //
    hoveredContact: number = -1;
    isContactSelected(contactId: number): boolean {
        return this.hoveredContact === contactId;
    }

    onContactHover(contactIndex: number): void {
        this.hoveredContact = contactIndex;
    }

    clearHoveredContact(): void {
        this.hoveredContact = -1;
    }

    addContact(): void {
        const contactArray = this.formGroup()?.get('properties').get('contacts') as FormArray;
        contactArray.push(this.editorModel().createChildContact(contactArray.length));
    }

    removeContact(contactIndex: number): void {
        (this.formGroup()?.get('properties').get('contacts') as FormArray).removeAt(contactIndex);
        this.reApplyPropertiesValidators.emit();
        // PlanEditorModel.reApplyPropertiesValidators(
        //     {
        //         formGroup: this.formGroup(),
        //         validationErrorModel: this.editorModel().validationErrorModel,
        //         blueprint: this.blueprint()
        //     }
        // );
        this.formGroup()?.get('properties').get('contacts').markAsDirty();
    }

    onContactKeyDown($event: KeyboardEvent, index: number, itemName: string){
        const contactsFormArray = (this.formGroup()?.get('properties').get('contacts') as FormArray);
        this.dragAndDropService.onKeyDown({
            $event,
            currentIndex: index,
            itemName,
            listLength: contactsFormArray.length,
            moveDownFn:() => {
                this.dropContacts({
                    previousIndex: index,
                    currentIndex: index + 1
                });
                setTimeout(() => document.getElementById('drag-handle-' + (index + 1))?.focus());
            },
            moveUpFn: () => { 
                this.dropContacts({
                    previousIndex: index,
                    currentIndex: index - 1
                });
                setTimeout(() => document.getElementById('drag-handle-' + (index - 1))?.focus());
            }
        })
    }

    dropContacts(event: {previousIndex: number, currentIndex: number}) {
        const contactsFormArray = (this.formGroup()?.get('properties').get('contacts') as FormArray);

        moveItemInArray(contactsFormArray.controls, event.previousIndex, event.currentIndex);
        contactsFormArray.updateValueAndValidity();
        this.reApplyPropertiesValidators.emit();
        // PlanEditorModel.reApplyPropertiesValidators(
        //     {
        //         formGroup: this.formGroup(),
        //         validationErrorModel: this.editorModel().validationErrorModel,
        //         blueprint: this.blueprint()
        //     }
        // );
        this.formGroup()?.get('properties').get('contacts').markAsDirty();
    }

    searchContact(contactIndex: number, field: FieldInSection): void {
        const dialogRef = this.dialog.open(PlanContactPrefillDialogComponent, {
            maxWidth: '700px',
            maxHeight: '80vh',
            data: {
                label: field.label
            }
        });
        dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result: PlanAssociatedUser) => {
            if (result) {
                const contactFormGroup = (this.formGroup()?.get('properties').get('contacts') as FormArray).at(contactIndex);
                contactFormGroup.get('firstName').patchValue(result?.name);
                contactFormGroup.get('lastName').patchValue(null);
                contactFormGroup.get('email').patchValue(result?.email);
                
                this.uiNotificationService.snackBarNotification(this.language.instant('PLAN-EDITOR.SNACK-BAR.SUCCESSFUL-PLAN-CONTACT'), SnackBarNotificationLevel.Success);
            }
        });
    }

    // getFieldFormControl(field: FieldInSection): FormGroup | FormArray | FormControl {
    //     if(!field){ return; }
    //     const emptyFormControl = new FormControl(null, field.required ? Validators.required : []);
    //     const emptyFormArray = new FormArray([], field.required ? Validators.required : []);
    //     const previewMode = !this.formGroup()
    //     const fieldId = field.id.toString();
    //     switch(field.category){
    //         case PlanBlueprintFieldCategory.System: {
    //             switch((field as SystemFieldInSection).systemFieldType){
    //                 case PlanBlueprintSystemFieldType.AccessRights: {
    //                     return !previewMode ? this.formGroup()?.get('accessType') as FormControl : emptyFormControl;
    //                 }
    //                 case PlanBlueprintSystemFieldType.Contact: {
    //                     return !previewMode ? this.formGroup()?.get('properties')?.get('contacts') as FormArray : emptyFormArray;
    //                 }
    //                 case PlanBlueprintSystemFieldType.Description: {
    //                     return !previewMode ? this.formGroup()?.get('description') as FormControl : emptyFormControl;
    //                 }
    //                 case PlanBlueprintSystemFieldType.Language: {
    //                     return !previewMode ? this.formGroup()?.get('language') as FormControl : emptyFormControl;
    //                 }
    //                 case PlanBlueprintSystemFieldType.Title: {
    //                     return !previewMode ? this.formGroup()?.get('label') as FormControl : emptyFormControl;
    //                 }
    //                 case PlanBlueprintSystemFieldType.User: {
    //                     return !previewMode ? this.formGroup()?.get('users') as FormArray : emptyFormControl;
    //                 }
    //             }
    //         }
    //         case PlanBlueprintFieldCategory.Extra: {
    //             switch((field as ExtraFieldInSection).dataType){
    //                 case PlanBlueprintExtraFieldDataType.Date: {
    //                     return !previewMode ? this.formGroup()?.get('properties')?.get('planBlueprintValues')?.get(fieldId)?.get('dateValue') as FormControl : emptyFormControl;
    //                 }
    //                 case PlanBlueprintExtraFieldDataType.Number: {
    //                     return !previewMode ? this.formGroup()?.get('properties')?.get('planBlueprintValues')?.get(fieldId)?.get('numberValue') as FormControl : emptyFormControl;
    //                 }
    //                 case PlanBlueprintExtraFieldDataType.RichText:
    //                 case PlanBlueprintExtraFieldDataType.Text: {
    //                     return !previewMode ? this.formGroup()?.get('properties')?.get('planBlueprintValues')?.get(fieldId)?.get('fieldValue') as FormControl : emptyFormControl;
    //                 }
    //             }
    //         }
    //         case PlanBlueprintFieldCategory.ReferenceType: {
    //             return (field as ReferenceTypeFieldInSection).multipleSelect ? 
    //                 (!previewMode ? this.formGroup()?.get('properties')?.get('planBlueprintValues')?.get(fieldId)?.get('references') as FormArray : emptyFormArray) : 
    //                 (!previewMode ? this.formGroup()?.get('properties')?.get('planBlueprintValues')?.get(fieldId)?.get('reference') as FormControl : emptyFormControl)
    //         }
    //     }
    // }
}
