import { ReferenceType } from "../reference-type/reference-type";

export interface DashboardStatistics {
	planCount: number;
	descriptionCount: number;
	referenceTypeStatistics: DashboardReferenceTypeStatistics[];
}

export interface DashboardReferenceTypeStatistics {
	count: number;
	referenceType: ReferenceType
}