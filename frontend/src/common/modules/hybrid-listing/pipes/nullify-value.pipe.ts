import { Pipe, PipeTransform } from '@angular/core';


const nullSet = new Set([null, undefined]);


@Pipe({
  name: 'nullifyValue'
})
export class NullifyValuePipe implements PipeTransform {

  transform(value: any, returnSpace: boolean): any {
    if(!nullSet.has(value) || returnSpace){
      return value;
    }
    return '-'
  }

}
