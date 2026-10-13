// Mirrors the backend MaintenanceTask enum. The backend runs a batch in this order, whatever order the tasks are sent in.

export type MaintenanceGroupKey = 'INDEX' | 'TOUCH' | 'ACCOUNTING' | 'KPI';

export interface MaintenanceGroup {
	key: MaintenanceGroupKey;
	tasks: string[];
}

export const AFTER_UPGRADE_PRESET = 'after-upgrade';

export const MAINTENANCE_GROUPS: MaintenanceGroup[] = [
	{ key: 'INDEX', tasks: ['elastic-index'] },
	{ key: 'TOUCH', tasks: ['tenant-touch', 'user-touch', 'description-touch', 'plan-touch'] },
	{
		key: 'ACCOUNTING', tasks: [
			'plan-accounting', 'description-accounting', 'plan-blueprint-accounting', 'plan-blueprint-type-accounting',
			'description-template-accounting', 'description-template-type-accounting', 'prefilling-source-accounting',
			'reference-accounting', 'reference-type-accounting', 'plan-status-accounting', 'description-status-accounting',
			'language-accounting', 'user-accounting', 'plan-evaluation-accounting', 'description-evaluation-accounting'
		]
	},
	{
		key: 'KPI', tasks: [
			'indicator-create', 'indicator-reset', 'indicator-access', 'dashboard-touch',
			'indicator-point-plan', 'indicator-point-description', 'indicator-point-reference', 'indicator-point-user',
			'indicator-point-plan-blueprint', 'indicator-point-description-template', 'indicator-point-tenant',
			'indicator-point-plan-blueprint-type', 'indicator-point-description-template-type'
		]
	}
];

export const INDICATOR_RESET_TASK = 'indicator-reset';

export function isIndicatorPointTask(task: string): boolean {
	return task.startsWith('indicator-point-');
}

export function taskLabelKey(task: string): string {
	return 'MAINTENANCE-TASKS.TASKS.' + task.toUpperCase();
}
