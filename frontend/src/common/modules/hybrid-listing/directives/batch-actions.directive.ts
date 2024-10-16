import {Directive, Input} from '@angular/core';


@Directive({
    selector: '[batch-actions]',
    exportAs: 'batchActions'
})
export class BatchActionsDirective{

    @Input()
    rowIdentity:(item: any) => any // how to identify rows between them


    public getSelections?:() => any[];


    public get selectedCount(): number{
        return this.getSelections?.()?.length || 0;
    }
}