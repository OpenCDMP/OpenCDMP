import { Component, effect, input, output, ViewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { FileUtils } from '@app/core/services/utilities/file-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { Guid } from '@common/types/guid';
import { Subscription, takeUntil } from 'rxjs';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@citesa/kpi-client/services';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { nameof } from 'ts-simple-nameof';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { MatError, MatFormFieldModule } from '@angular/material/form-field';
import { MatDropzone } from '@ngx-dropzone/material';
import { FileInputDirective, FileInputValidators, FileInputValue } from '@ngx-dropzone/cdk';
import { MatChipsModule } from '@angular/material/chips';
import FileSaver from 'file-saver';

@Component({
  selector: 'app-file-uploader',
  imports: [MatDropzone, CommonModule, MatButtonModule, MatIconModule, TranslateModule, MatFormFieldModule, ReactiveFormsModule, MatError, FileInputDirective , MatChipsModule],
  templateUrl: './file-uploader.component.html',
  styleUrl: './file-uploader.component.scss'
})
 
export class FileUploadComponent extends BaseComponent{
    //TODO: (mchouliara) remove deprecated ngx-dropzone (3.0.0)
    @ViewChild(MatDropzone) dropZone: MatDropzone;
    static nextId = 0;

    multiple = false; //TODO (mchouliara) check if we want to apply for multiple file uploads
    hidePreview = input<boolean>(false);
    placeholder = input<string>(null);
    label = input<string>(null);
    removable = input<boolean>(true);
    id = input<string>(`file-upload-${FileUploadComponent.nextId++}`);
    storageFileIdControl = input<FormControl<string | Guid>>(null);
    initFile = input<StorageFile>();

    accept = input.required<string>();
    maxSize = input<number>();
    minSize = input<number>();
    required = input<boolean>(false);
    
    onChange = output<FileInputValue>();
    onRemove = output();
    
    preUploadCheckFn = input<(fileInput: FileInputValue) => boolean>(null);

    fileCtrl: FormControl<BaseFileValues>;
    fileCtrlObserver: Subscription;

    requiredValidator = Validators.required;
    constructor(
        private storageFileService: StorageFileService,
        private fileUtils: FileUtils,
        private httpErrorHandlingService: HttpErrorHandlingService,
        private uiNotificationService: UiNotificationService,
        private language: TranslateService
    ){
        super();
        effect(() => {
            const accept = this.accept();
            const maxSize = this.maxSize();
            const minSize = this.minSize();
            const form = this.storageFileIdControl();
            const initFile = this.initFile();

            this.fileCtrl = new FormControl<BaseFileValues>(null);
            if(accept){
                this.fileCtrl.addValidators(FileInputValidators.accept(accept));
            }
            if(maxSize){
                this.fileCtrl.addValidators(this.sizeNullOrValid(FileInputValidators.maxSize(maxSize)));
            }
            if(minSize){
                this.fileCtrl.addValidators(this.sizeNullOrValid(FileInputValidators.minSize(minSize)));
            }
            if(this.required()){
                this.fileCtrl.addValidators(Validators.required)
            }

            if(initFile) {
                this.fileCtrl.setValue({
                    name: initFile.fullName,
                    type: initFile.mimeType
                }, {emitEvent: false})
            }else if(form?.value){
                const fields = [
                    nameof<StorageFile>(x => x.id),
                    nameof<StorageFile>(x => x.name),
                    nameof<StorageFile>(x => x.mimeType),
                    nameof<StorageFile>(x => x.extension),
                ]
                this.storageFileService.getSingle(Guid.parse(form.value as string), fields).pipe(takeUntil(this._destroyed)).subscribe(storageFile => {
                    this.fileCtrl.patchValue({
                        name: storageFile?.name + '.' + storageFile?.extension,
                        type: storageFile?.mimeType
                    }, {emitEvent: false});
                });
            }
            if(form?.disabled){
                this.fileCtrl.disable();
            }
            this.fileCtrlObserver?.unsubscribe();
            this.fileCtrlObserver = this.fileCtrl.valueChanges.pipe(takeUntil(this._destroyed))
            .subscribe((value) => {
                this.fileChangeEvent(value as File);
            })

        })
    }

    // errors = new Set([]);
    fileChangeEvent(file: File) {
        if(this.fileCtrl.valid){
            const fileInput = file;
            if(this.storageFileIdControl()){
                const preUploadPassed = this.preUploadCheckFn() ? this.preUploadCheckFn()(fileInput) : true;
                if(preUploadPassed){
                    this.storageFileService.uploadTempFiles(fileInput)
                    .pipe(takeUntil(this._destroyed)).subscribe({
                        next: (response) => {
                            this.storageFileIdControl().patchValue(response[0].id.toString());
                            this.storageFileIdControl().markAsTouched();
                            this.onChange.emit(fileInput);
                        }, 
                        error: (error) => {
                            this.onCallbackUploadFail(error.error);
                        }
                    })
                }
            } else {
                this.onChange.emit(fileInput);
            }
        } else {
            this.storageFileIdControl()?.patchValue(null);
            this.onRemove.emit();
        }
	}

    // private createFileNameDisplay(name: string, extension: string) {
	// 	if (extension.startsWith('.')) this.fileNameDisplay.set(name + extension);
	// 	else this.fileNameDisplay.set(name + '.' + extension);
	// }

    private onCallbackUploadFail(error: any) {
        this.remove();
        this.uiNotificationService.snackBarNotification(this.language.instant(error.message), SnackBarNotificationLevel.Error);
    }

    
    remove(){
        this.fileCtrl.reset(null, {emitEvent: false});
        this.storageFileIdControl()?.setValue(null);
        this.storageFileIdControl()?.markAsTouched();
        this.onRemove.emit();
    }

    download(){
        if(!this.storageFileIdControl()?.value){
            return;
        }
        const id = Guid.parse((this.storageFileIdControl().value as string));

        this.storageFileService.download(id).pipe(takeUntil(this._destroyed))
        .subscribe({
            next: (response) => {
                const blob = new Blob([response.body]);
                const filename = this.fileUtils.getFilenameFromContentDispositionHeader(response.headers.get('Content-Disposition'));
                FileSaver.saveAs(blob, filename);
            },
            error: (error) => this.httpErrorHandlingService.handleBackedRequestError(error)
        });
    }

    //added a little spice to the base validators- do not check for size if is null
    private sizeNullOrValid(sizeValidator: ValidatorFn): ValidatorFn {
        return (control: FormControl<BaseFileValues>): { [key: string]: any } => {
            if(!control?.value?.size){
                return;
            }
            return sizeValidator(control);
        };
    }

    get maxFileSizeInMB(): number {
        return this.maxSize() ? this.maxSize() / 1048576 : null;
    }

    public openFilePicker(){
        this.dropZone?.openFilePicker();
    }
}

interface BaseFileValues extends Partial<File> {
    name: string;
    type: string;
}