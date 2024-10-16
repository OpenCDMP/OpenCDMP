import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ResponseErrorCode, ResponseErrorCodeHelper } from '@app/core/common/enum/respone-error-code';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { TranslateService } from '@ngx-translate/core';

@Injectable()
export class HttpErrorHandlingService {
	constructor(
		protected language: TranslateService,
		protected uiNotificationService: UiNotificationService,
	) {
	}

	handleBackedRequestError(errorResponse: HttpErrorResponse, messageOvverrides?: Map<number, string>, defaultNotificationLevel: SnackBarNotificationLevel = SnackBarNotificationLevel.Warning) {
		const error: HttpError = this.getError(errorResponse);
		let errorMessage = messageOvverrides?.has(error.statusCode) ? messageOvverrides?.get(error.statusCode) : null;

		if(errorResponse.error && ResponseErrorCodeHelper.isBackendError(errorResponse.error?.code)){
			if (errorResponse.error.code === ResponseErrorCode.UsageLimitException && errorResponse.error.error){
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.BACKEND-ERRORS.USAGE-LIMIT-EXCEPTION', { 'usageLimitLabel': errorResponse.error.error }), SnackBarNotificationLevel.Error);
			} else if (errorResponse.error.code === ResponseErrorCode.descriptionTemplateTypeImportNotFound){
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.BACKEND-ERRORS.DESCRIPTION-TEMPLATE-TYPE-IMPORT-NOT-FOUND', { 'descriptionTemplateTypeLabel': errorResponse.error.error }), SnackBarNotificationLevel.Error);
			} else if (errorResponse.error.code === ResponseErrorCode.referenceTypeImportNotFound){
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.BACKEND-ERRORS.REFERENCE-TYPE-IMPORT-NOT-FOUND', { 'referenceTypeCode': errorResponse.error.error }), SnackBarNotificationLevel.Error);
			} else if (errorResponse.error.code === ResponseErrorCode.descriptionTemplateImportNotFound){
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.BACKEND-ERRORS.DESCRIPTION-TEMPLATE-IMPORT-NOT-FOUND', { 'descriptionTemplateLabel': errorResponse.error.error }), SnackBarNotificationLevel.Error);
			} else if (errorResponse.error.code === ResponseErrorCode.planBlueprintImportNotFound){
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.BACKEND-ERRORS.PLAN-BLUEPRINT-IMPORT-NOT-FOUND', { 'planBlueprintLabel': errorResponse.error.error }), SnackBarNotificationLevel.Error);
			} else if (errorResponse.error.code === ResponseErrorCode.blueprintDescriptionTemplateImportDraft){
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.BACKEND-ERRORS.BLUEPRINT-DESCRIPTION-TEMPLATE-IMPORT-DRAFT', { 'descriptionTemplateLabel': errorResponse.error.error }), SnackBarNotificationLevel.Error);
			} else if (errorResponse.error.code === ResponseErrorCode.planDescriptionTemplateImportDraft){
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.BACKEND-ERRORS.PLAN-DESCRIPTION-TEMPLATE-IMPORT-DRAFT', { 'descriptionTemplateLabel': errorResponse.error.error }), SnackBarNotificationLevel.Error);
			} else if (errorResponse.error.code === ResponseErrorCode.descriptionTemplateTypeImportDraft){
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.BACKEND-ERRORS.DESCRIPTION-TEMPLATE-TYPE-IMPORT-DRAFT', { 'descriptionTemplateTypeCode': errorResponse.error.error }), SnackBarNotificationLevel.Error);
			} else if (errorResponse.error.code === ResponseErrorCode.planBlueprintImportDraft){
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.BACKEND-ERRORS.PLAN-BLUEPRINT-IMPORT-DRAFT', { 'planBlueprintCode': errorResponse.error.error }), SnackBarNotificationLevel.Error);
			} else {
				this.uiNotificationService.snackBarNotification(ResponseErrorCodeHelper.getErrorMessageByBackendStatusCode(errorResponse.error.code, this.language), SnackBarNotificationLevel.Error);
			}
		}
		else if (error.statusCode === 302) {
			errorMessage ??= this.language.instant('GENERAL.SNACK-BAR.REDIRECT');
			this.uiNotificationService.snackBarNotification(errorMessage, SnackBarNotificationLevel.Warning);
		}
		else if (error.statusCode === 400) {
			errorMessage ??= this.language.instant('GENERAL.SNACK-BAR.BAD-REQUEST');
			this.uiNotificationService.snackBarNotification(errorMessage, SnackBarNotificationLevel.Warning);
		}
		else if (error.statusCode === 403) {
			errorMessage ??= this.language.instant('GENERAL.SNACK-BAR.FORBIDDEN');
			this.uiNotificationService.snackBarNotification(errorMessage, SnackBarNotificationLevel.Warning);
		}
		else if (error.statusCode === 404) {
			errorMessage ??= this.language.instant('GENERAL.SNACK-BAR.NOT-FOUND');
			this.uiNotificationService.snackBarNotification(errorMessage, SnackBarNotificationLevel.Warning);
		}
		else {
			errorMessage = messageOvverrides?.has(-1) ? 
				messageOvverrides?.get(error.statusCode) : this.language.instant('GENERAL.SNACK-BAR.GENERIC-ERROR');
			this.uiNotificationService.snackBarNotification(errorMessage, defaultNotificationLevel);
		}
	}

	getError(errorResponse: HttpErrorResponse): HttpError {
		const error: HttpError = new HttpError();
		error.statusCode = errorResponse.status;
		error.messages = this.parseMessages(error.statusCode, errorResponse);
		try {
			error.errorCode = + errorResponse.error.code;
		} catch { }
		return error;
	}

	private parseMessages(httpStatusCode: number, errorPayload: any): string[] {
		const result: string[] = [];
		switch (httpStatusCode) {
			case 400: // Bad Request, Used for validation errors.
				if (errorPayload && errorPayload.error && errorPayload.error.error_description) { result.push(errorPayload.error.error_description); }
				else if (errorPayload && errorPayload.error && errorPayload.error.error) { result.push(errorPayload.error.error); }
				else if (errorPayload && errorPayload.message) { result.push(errorPayload.message); }
				break;
			case 404: // Not Found
				if (errorPayload && errorPayload.error && errorPayload.error.error_description) { result.push(errorPayload.error.error_description); }
				else if (errorPayload && errorPayload.error && errorPayload.error.error) { result.push(errorPayload.error.error); }
				else if (errorPayload && errorPayload.message) { result.push(errorPayload.message); }
				break;
			case 500: // Bad Request, Used for validation errors.
				if (errorPayload && errorPayload.error && errorPayload.error.error_description) { result.push(errorPayload.error.error_description); }
				else if (errorPayload && errorPayload.error && errorPayload.error.error) { result.push(errorPayload.error.error); }
				else if (errorPayload && errorPayload.message) { result.push(errorPayload.message); }
				break;
			default:
				if (errorPayload && errorPayload.error && errorPayload.error.error_description) { result.push(errorPayload.error.error_description); }
				else if (errorPayload && errorPayload.error && errorPayload.error.error) { result.push(errorPayload.error.error); }
				else if (errorPayload && errorPayload.message) { result.push(errorPayload.message); }
				break;
		}

		if (result.length === 0) { result.push(this.language.instant('COMMONS.ERRORS.DEFAULT')); }

		return result;
	}
}

export class HttpError {
	statusCode: number;
	messages: string[];
	errorCode: number;

	getMessagesString(): string {
		return this.messages ? this.messages.join(', ') : null;
	}
}
