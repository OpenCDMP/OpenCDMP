import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
    name: 'invokeMemoized',
    standalone: false
  })
  export class InvokeMemoizedPipe implements PipeTransform {
  
    transform(fn: (...any) => any, ...params: any[]): any {
        return fn?.(...params);
    }
  
  }