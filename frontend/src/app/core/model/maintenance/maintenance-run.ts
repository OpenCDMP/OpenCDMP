import { MaintenanceRunStatus } from '@app/core/common/enum/maintenance-run-status';
import { MaintenanceTaskRunStatus } from '@app/core/common/enum/maintenance-task-run-status';

export interface MaintenanceTaskRun {
	task: string;
	status: MaintenanceTaskRunStatus;
	startedAt?: Date;
	finishedAt?: Date;
	error?: string;
}

export interface MaintenanceRun {
	id: string;
	preset?: string;
	status: MaintenanceRunStatus;
	stopOnFailure: boolean;
	stopRequested: boolean;
	createdAt: Date;
	finishedAt?: Date;
	tasks: MaintenanceTaskRun[];
}

export interface MaintenanceRunPersist {
	tasks: string[];
	stopOnFailure: boolean;
	preset?: string;
}

export interface DatabaseMigration {
	version?: string;
	description: string;
	type: string;
	installedOn?: Date;
	executionTime?: number;
	state: string;
}

export interface DatabaseMigrationInfo {
	enabled: boolean;
	currentVersion?: string;
	applied?: number;
	pending?: number;
	failed?: number;
	migrations?: DatabaseMigration[];
}

export interface InstallationNotice {
	title: string;
	body?: string;
	link?: string;
}

export interface InstallationInfo {
	id: string;
	version?: string;
	updateCheckEnabled: boolean;
	shareUsage: boolean;
	checkedAt?: Date;
	latestVersion?: string;
	releaseUrl?: string;
	updateAvailable?: boolean;
	notices?: InstallationNotice[];
}

export interface InstallationPersist {
	shareUsage: boolean;
}
