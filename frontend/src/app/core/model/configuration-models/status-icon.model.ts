import { InternalStatus } from "@annotation-service/core/enum/internal-status.enum";

export class StatusIcon {
  private _internalStatus: InternalStatus;
  get InternalStatus(): InternalStatus {
    return this._internalStatus;
  }

  private _icon: string;
  get icon(): string {
    return this._icon;
  }
  
  private _id: string;
  get id(): string {
    return this._id;
  }

  public static parseValue(value: any): StatusIcon {
		const obj: StatusIcon = new StatusIcon();
		obj._internalStatus = value.internalStatus;
		obj._icon = value.icon;
		obj._id = value.id;
		return obj;
	}
}
