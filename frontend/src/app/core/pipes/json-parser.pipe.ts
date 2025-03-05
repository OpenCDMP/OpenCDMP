import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
    name: 'jsonParser',
    standalone: false
})
export class JsonParserPipe implements PipeTransform {

	transform(val) {
		if (typeof val === 'string') {
			return JSON.parse(val)
		}
		else {
			return val;
		}
	}
}
