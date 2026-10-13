import { DepositFileKind } from "@app/core/common/enum/deposit-file-kind";

export interface DepositFile {
	kind: DepositFileKind;
	filename: string;
	/** For the supporting files archive: the files inside it. */
	contents?: string[];
}
