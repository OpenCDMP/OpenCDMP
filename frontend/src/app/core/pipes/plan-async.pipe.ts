import { Pipe, PipeTransform } from "@angular/core";
import { Guid } from "@common/types/guid";
import { Observable, of, catchError } from "rxjs";
import { nameof } from "ts-simple-nameof";
import { Plan } from "../model/plan/plan";
import { PlanService } from "../services/plan/plan.service";

@Pipe({
  name: 'planAsync',
  standalone: true
})
export class PlanAsyncPipe implements PipeTransform {

    constructor(private planService: PlanService){

    }
    transform(id: string): Observable<Plan> {
        if(!id){
            return of(null);
        }
        return this.planService.getSingle(Guid.parse(id), [nameof<Plan>(x => x.label)])
        .pipe(catchError((error) => of(null)));
    }

}