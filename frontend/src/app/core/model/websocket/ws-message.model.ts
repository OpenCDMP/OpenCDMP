import { WSActionType } from '@app/core/common/enum/ws-action-type';
import { Guid } from '@common/types/guid';
import { User } from '../user/user';

export interface WSMessage {
	producer: User;
	actionType: WSActionType;
	payload: any;
}

export interface UserActionPayload {
	blueprintSectionId?: Guid;
	blueprintFieldId?: Guid;
	descriptionId?: Guid;
	descriptionPageId?: string;
	descriptionSectionId?: string;
	descriptionFieldSetId?: string;
}

