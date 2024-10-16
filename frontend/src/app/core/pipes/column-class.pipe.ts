import {Pipe, PipeTransform} from "@angular/core";

@Pipe({
	name: 'columnClass'
})
export class ColumnClassPipe implements PipeTransform{

	transform(value: number): any {
		return 'col-' + Math.ceil(12/value);
	}
}
