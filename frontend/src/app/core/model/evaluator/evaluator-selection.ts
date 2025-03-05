import { ValueSet } from "./evaluator-value-set"; 

export class SelectionConfiguration {
    valueSetList: ValueSet[];
  
    constructor(valueSetList: ValueSet[]) {
      this.valueSetList = valueSetList;
    }
}