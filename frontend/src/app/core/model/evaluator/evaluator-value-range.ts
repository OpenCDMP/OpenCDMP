import { NumberType } from "./evaluator-number-type.model";

export class ValueRangeConfiguration {
    numberType: NumberType;
    min: number;
    max: number;
    minPassValue: number;
  
    constructor(numberType: NumberType, min: number, max: number, minPassValue: number) {
      this.numberType = numberType;
      this.min = min;
      this.max = max;
      this.minPassValue = minPassValue;
    }
}