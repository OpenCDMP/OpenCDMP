import { Pipe, PipeTransform } from '@angular/core';
import { TableColumnProp } from '@common/modules/hybrid-listing/hybrid-listing.component';
import { string } from 'yargs';


@Pipe({
  name: 'visibleFields'
})
export class VisibleFieldsPipe implements PipeTransform {

  transform(value: Record<TableColumnProp,any>, visibleColumns: TableColumnProp[]): Record<TableColumnProp, any> {




    if(!visibleColumns || !value){
      return value;
    }

    if(!(typeof value === 'object')){
      return value;
    }

    const clone = JSON.parse(JSON.stringify(value));

    this.cleanUpRecursively(clone, '', visibleColumns);

    return clone;

  }


  private cleanUpRecursively(target: any, currentPath: string, whitelisted: TableColumnProp[]){


    if(!target || !whitelisted){
      return;
    }

    if(!(typeof target === 'object')){
      return 
    }

    for(const [key,value] of Object.entries(target)){

      const nextPath = `${currentPath}${key}`;

      const isWhitelisted = whitelisted.some(x => (x === nextPath) ||  (typeof x === 'string' && x.startsWith(nextPath)));

      if(!isWhitelisted){
        delete target[key];
        continue;
      }
      this.cleanUpRecursively(value, `${nextPath}.`, whitelisted);
    }
  }

}
