import { catchError, map, Observable, of } from "rxjs";
import { Description } from "../model/description/description";
import { DescriptionService } from "../services/description/description.service";
import { nameof } from "ts-simple-nameof";
import { Pipe, PipeTransform } from "@angular/core";
import { Guid } from "@common/types/guid";

@Pipe({
  name: 'descriptionAsync',
  standalone: true
})
export class DescriptionAsyncPipe implements PipeTransform {

    constructor(private descriptionService: DescriptionService){

    }
    transform(id: string): Observable<Description> {
        if(!id){
            return of(null);
        }
        return this.descriptionService.getSingle(Guid.parse(id), [nameof<Description>(x => x.label)])
        .pipe(catchError((error) => of(null)));
    }

}