import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'initialLetter',
  standalone: true
})
export class InitialLetterPipe implements PipeTransform {

  transform(val: string): string {
    if(!val?.length){
        return;
    }
    return val.charAt(0).toUpperCase();
  }

}
