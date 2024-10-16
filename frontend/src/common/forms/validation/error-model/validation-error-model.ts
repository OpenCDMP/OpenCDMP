import { Serializable } from '@common/types/json/serializable';

export class ValidationErrorModel implements Serializable<ValidationErrorModel> {
	public error: string;
	private message: Array<ErrorMessageItem>;

	public fromJSONObject(item: any): ValidationErrorModel {
		this.error = item.error;
		this.message = item.message;
		return this;
	}

	public getErrors(propertyNames: string[]): string {
		const errors: string[] = [];
		propertyNames.forEach(propertyName => {
			const error = this.getError(propertyName);
			if (error) { errors.push(error); }
		});
		return errors.join(', ');
	}

	public getError(propertyName: string): string {
		let error: string;
		if (this.message && Array.isArray(this.message)) {
			for (const element of this.message) {
				if (element.key === propertyName) {
					error = element.value.join(', ');
					break;
				}
			}
		}
		return error;
	}

	// errors by array index
	public getErrorForArray(arrayProperty: string, fieldProperty: string): Map<number, string> {
		const regExp = new RegExp(`^${arrayProperty}\\[([0-9]+)\\]\\.${fieldProperty}$`); // 1st group is index
		const errors = new Map<number, string>();
		if (this.message && Array.isArray(this.message)) {
			this.message.forEach(element => {
				const match = element.key.match(regExp);
				if (match && match.length >= 2) {
					const index = Number.parseInt(match[1]);
					errors.set(index, element.value.join(', '));
				}
			});
		}
		return errors;
	}

	public setError(propertyName: string, error: string) {
		if (this.message  && Array.isArray(this.message)) {
			let exists = false;
			for (const element of this.message) {
				if (element.key === propertyName) {
					if (!element.value.includes(error)) { element.value.push(error); }
					exists = true;
					break;
				}
			}
			if (!exists) { this.message.push({ key: propertyName, value: [error] }); }
		} else {
			this.message = [{ key: propertyName, value: [error] }];
		}
	}

	public clear() {
		this.error = undefined;
		if (this.message && Array.isArray(this.message)) {
			this.message.forEach(element => {
				element.value.splice(0);
			});
		}
	}

	public clearPart(prefix: string) {
		if (this.message && Array.isArray(this.message)) {
			this.message.forEach(element => {
				if (element && element.key && element.key.startsWith(prefix)) {
					element.value.splice(0);
				}
			});
		}
	}
}

class ErrorMessageItem {
	key: string;
	value: Array<string> = [];
}
