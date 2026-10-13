import { Component, computed, effect, input, output, ChangeDetectionStrategy } from '@angular/core';
import { FormArray, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PlanContactEditorModel, PlanContactForm, PlanEditorModel } from '../plan-editor-blueprint/plan-editor.model';
import { MatDialog } from '@angular/material/dialog';
import { moveItemInArray, CdkDropList, CdkDrag, CdkDragHandle } from '@angular/cdk/drag-drop';
import { PlanContactPrefillDialogComponent } from './plan-contact-prefill-dialog/plan-contact-prefill-dialog.component';
import { BaseField } from '@app/core/model/description-template/description-template';
import { isFieldRequired } from '@app/ui/description/editor/description-form/field-required.helper';
import { BaseComponent } from '@common/base/base.component';
import { DragAndDropAccessibilityService } from '@app/core/services/accessibility/drag-and-drop-accessibility.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { takeUntil } from 'rxjs';
import { PlanAssociatedUser } from '@app/core/model/user/user';
import { MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatFormField, MatLabel, MatError } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatTooltip } from '@angular/material/tooltip';

@Component({
    selector: 'app-plan-contact-editor',
    templateUrl: './plan-contact-editor.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [CdkDropList, CdkDrag, MatIconButton, CdkDragHandle, MatIcon, MatFormField, MatLabel, MatInput, FormsModule, ReactiveFormsModule, MatError, MatTooltip, TranslatePipe]
})
export class PlanContactEditorComponent extends BaseComponent{
    contactsFormArray = input<FormArray<PlanContactForm>>();
    editorModel = input<PlanEditorModel>();
    canEdit = input<boolean>(false);
    reApplyPropertiesValidators = output();
    path = input<string>();
    field = input<BaseField>();

    reorderAssistiveText = computed(() => this.dragAndDropService.assistiveTextSignal());

    constructor(
        private dialog: MatDialog,
        private uiNotificationService: UiNotificationService,
        private language: TranslateService,
        protected dragAndDropService: DragAndDropAccessibilityService
    ) {
        super();
        // A required Contact field with no rows shows only the "+" button, which reads as "nothing to
        // fill in" rather than "click here first". Seed one empty row so the inputs are visible
        // immediately; only once per empty array, so removing the last row afterwards doesn't bring it back.
        let seeded = false;
        effect(() => {
            const contactArray = this.contactsFormArray();
            if (!contactArray) { return; }
            if (contactArray.length > 0) { seeded = false; return; }
            if (seeded || !this.canEdit() || !isFieldRequired(this.field())) { return; }
            seeded = true;
            this.addContact();
        });
    }

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
        const contactArray = this.contactsFormArray();
        const contact: PlanContactEditorModel = new PlanContactEditorModel(this.editorModel()?.validationErrorModel);
        contactArray.push(contact.buildForm({ rootPath: 'properties.contacts[' + contactArray.length + '].' }));
    }

    removeContact(contactIndex: number): void {
        (this.contactsFormArray()).removeAt(contactIndex);
        this.reApplyPropertiesValidators.emit();
        // PlanEditorModel.reApplyPropertiesValidators(
        //     {
        //         formGroup: this.formGroup(),
        //         validationErrorModel: this.editorModel().validationErrorModel,
        //         blueprint: this.blueprint()
        //     }
        // );
        this.contactsFormArray().markAsDirty();
    }

    onContactKeyDown($event: KeyboardEvent, index: number, itemName: string) {
        const contactsFormArray = (this.contactsFormArray());
        this.dragAndDropService.onKeyDown({
            $event,
            currentIndex: index,
            itemName,
            listLength: contactsFormArray.length,
            moveDownFn: () => {
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

    dropContacts(event: { previousIndex: number, currentIndex: number }) {
        const contactsFormArray = (this.contactsFormArray());

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
        this.contactsFormArray().markAsDirty();
    }

    searchContact(contactIndex: number): void {
        const dialogRef = this.dialog.open(PlanContactPrefillDialogComponent, {
            maxWidth: '700px',
            maxHeight: '80vh',
            data: {
                label: this.field().data?.label
            }
        });
        dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result: PlanAssociatedUser) => {
            if (result) {
                const contactFormGroup = (this.contactsFormArray()).at(contactIndex);
                contactFormGroup.get('firstName').patchValue(result?.name);
                contactFormGroup.get('lastName').patchValue(null);
                contactFormGroup.get('email').patchValue(result?.email);

                this.uiNotificationService.snackBarNotification(this.language.instant('PLAN-EDITOR.SNACK-BAR.SUCCESSFUL-PLAN-CONTACT'), SnackBarNotificationLevel.Success);
            }
        });
    }
}
