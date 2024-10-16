import { Pipe, PipeTransform } from "@angular/core";


@Pipe({
  name: 'appTitleCaseTransform'
})
export class AppTitleCaseFormatPipe implements PipeTransform {

  transform(value: any, ...args: any[]) {
    return value.split('-').map(element => element.charAt(0).toUpperCase() + element.slice(1)).join(' ');
  }
} 