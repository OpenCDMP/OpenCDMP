import { Injectable } from '@angular/core';

@Injectable()
export class FileUtils {
	constructor() { }

	getFilenameFromContentDispositionHeader(header: string): string {
		const regex: RegExp = new RegExp(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/g);

		const matches = header.match(regex);
		let filename: string;
		for (let i = 0; i < matches.length; i++) {
			const match = matches[i];
			if (match.includes('filename="')) {
				filename = match.substring(10, match.length - 1);
				break;
			} else if (match.includes('filename=')) {
				filename = match.substring(9);
				break;
			}
		}
		return filename;
	}
}
