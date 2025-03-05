import { SuccessStatus } from "./evaluator-success-status.model";

export class ValueSet {
    key: number;
    successStatus: SuccessStatus;
  
    constructor(key: number, successStatus: SuccessStatus) {
      this.key = key;
      this.successStatus = successStatus;
    }
}