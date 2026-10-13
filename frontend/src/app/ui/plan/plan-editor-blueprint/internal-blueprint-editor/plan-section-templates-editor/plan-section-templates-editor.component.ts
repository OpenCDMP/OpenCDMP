import { Component, effect, input, Optional, output, ChangeDetectionStrategy } from '@angular/core';
import { DescriptionInfo } from '../../plan-temp-storage.service';
import { BaseComponent } from '@common/base/base.component';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { takeUntil } from 'rxjs';
import { Guid } from '@common/types/guid';
import { PlanLockService } from '../../plan-lock.service';
import { statusContrastColor } from '../../editor-session-state.service';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatTooltip } from '@angular/material/tooltip';
import { NgTemplateOutlet, DatePipe } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-plan-section-templates-editor',
    templateUrl: './plan-section-templates-editor.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './plan-section-templates-editor.component.scss',
    imports: [
        MatButton,
        MatIcon,
        MatTooltip,
        NgTemplateOutlet,
        MatIconButton,
        DatePipe,
        TranslatePipe,
    ],
})
export class PlanSectionTemplatesEditorComponent extends BaseComponent {
    descriptions = input<DescriptionInfo[]>([]);
    canEdit = input<boolean>(false);
    canDelete = input<boolean>(false);
    // Owner-only: whether the plan may curate this section's templates (blueprint allows it + edit permission).
    canManageTemplates = input<boolean>(false);
    // Mirrors the ToC's add-description guard (max multiplicity reached / templates touched).
    canAddDescription = input<boolean>(true);
    addDescriptionDisabledReason = input<string>();
    addDescription = output<void>();
    navigateToDescription = output<Guid>();
    removeDescription = output<DescriptionInfo>();
    manageTemplates = output<void>();

    constructor(
        private storageFileService: StorageFileService,
            private sanitizer: DomSanitizer,
            private httpErrorHandlingService: HttpErrorHandlingService,
            @Optional() private planLockService: PlanLockService
        ){
        super();
        effect(() => {
            if(this.descriptions()?.length){
                // template logos + status logos (FE-2) share the same storageFileMap
                this.downloadStorageFiles(this.descriptions().flatMap(x => [
                        x.lastPersist?.descriptionTemplate?.definition?.iconConfiguration?.storageFile?.id,
                        x.lastPersist?.status?.definition?.iconConfiguration?.storageFile?.id
                    ]).filter(x => !!x)
                )
            }
        })
    }

    // FE-2: readable text color for a status chip given its admin-configured background color.
    protected statusContrastColor = statusContrastColor;

    protected storageFileMap = new Map<Guid, SafeUrl>([]);
    protected downloadStorageFiles(storageFileIds: Guid[]){
        const newIds = new Set(storageFileIds.filter((id) => !this.storageFileMap.has(id)));
        newIds.forEach((id) => {
            this.storageFileService.download(id).pipe(takeUntil(this._destroyed))
            .subscribe({
                next: response => {
                    this.storageFileMap.set(id, this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(response.body)));
                },
                error: error => this.httpErrorHandlingService.handleBackedRequestError(error)
            });
        })
    }

    //** LOCKS */
    descriptionLocked(id: Guid): boolean {
        return this.planLockService?.externalLockIds()?.includes(id?.toString());
    }
}
