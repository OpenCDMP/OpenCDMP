import { Injectable } from '@angular/core';
import { ConfigurationService } from '../configuration/configuration.service';

export enum LogLevel {
	Error = 'error',
	Warning = 'warning',
	Info = 'info',
	Debug = 'debug',
}

export type LogOutput = (level: LogLevel, ...objects: any[]) => void;

@Injectable()
export class LoggingService {

	private loggingLevels: LogLevel[];
	private loggingOutputs: LogOutput[] = [];

	constructor(private configurationService: ConfigurationService) {

	}

	debug(...logs: any[]) {
		this.log(LogLevel.Debug, logs);
	}

	info(...logs: any[]) {
		this.log(LogLevel.Info, logs);
	}

	warn(...logs: any[]) {
		this.log(LogLevel.Warning, logs);
	}

	error(...logs: any[]) {
		this.log(LogLevel.Error, logs);
	}

	init() {
		this.loggingLevels = this.configurationService.logging.loglevels as LogLevel[];

		this.loggingOutputs.push((level: LogLevel, ...objects: any[]) => {
			switch (level) {
				case LogLevel.Debug:
					// tslint:disable-next-line:no-console
					console.debug(objects.join(', '));
					break;
				case LogLevel.Info:
					// tslint:disable-next-line:no-console
					console.info(objects.join(', '));
					break;
				case LogLevel.Warning:
					console.warn(objects.join(', '));
					break;
				case LogLevel.Error:
					console.error(objects.join(', '));
					break;
			}
		});
	}

	private log(level: LogLevel, logs: any[]) {
		if (!this.loggingLevels) { this.init(); }

		if (this.configurationService.logging.enabled && this.loggingLevels.includes(level)) {
			this.loggingOutputs.forEach((output) => output.apply(output, [level, logs]));
		}
	}
}
