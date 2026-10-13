import { ReferenceType } from "../reference-type/reference-type";

export interface DashboardStatistics {
	planCount: number;
	descriptionCount: number;
    planBlueprintsCount: number;
    descriptionTemplatesCount: number;
	referenceTypeStatistics: DashboardReferenceTypeStatistics[];
}

export interface DashboardReferenceTypeStatistics {
	count: number;
	referenceType: ReferenceType;
	/** A source of the type has a logo, served at `reference-type/public/{id}/logo`. */
	hasLogo: boolean;
}