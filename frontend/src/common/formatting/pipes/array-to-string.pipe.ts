import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
    name: 'arrayToString',
    standalone: false
})
export class ArrayToStringPipe implements PipeTransform {

	transform(list: any, path: string): string {
        let finalString = '';

		if(list && list.length > 0 ){
			const directions = path?.split('.');

			finalString = list.map((listItem: any) => {
				return directions?.reduce((item, currentDirection) => item?.[currentDirection] , listItem)
			})
        	.filter((x: any) => !!x)
        	.join(', ');
		}

		return finalString || null;
	}
}
