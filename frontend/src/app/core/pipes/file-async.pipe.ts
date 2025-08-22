import { Pipe, PipeTransform } from "@angular/core";
import { StorageFileService } from "../services/storage-file/storage-file.service";
import { catchError, map, Observable, of } from "rxjs";
import { StorageFile } from "../model/storage-file/storage-file";
import { Guid } from "@common/types/guid";
import { nameof } from "ts-simple-nameof";

@Pipe({
  name: 'fileAsync',
  standalone: true
})
export class FileAsyncPipe implements PipeTransform {

    constructor(private storageFileService: StorageFileService){

    }
    transform(id: string): Observable<string> {
        if(!id){
            return of(null);
        }
        return this.storageFileService.getSingle(Guid.parse(id), [nameof<StorageFile>(x => x.name)])
        .pipe(catchError((error) => of(null)), map((x) => x.name));
    }

}
