import { WSActionType } from '@app/core/common/enum/ws-action-type';
import { Guid } from '@common/types/guid';
import { User } from '../user/user';

export interface WSMessage {
	producer: User;
	actionType: WSActionType;
	payload: any;
}

export interface UserActionPayload {
	descriptionId?: Guid;
	sectionId?: string;
	fieldSetId?: string;
}

