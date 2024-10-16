import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { BehaviorSubject } from "rxjs";

@Injectable({
    providedIn:'root'
})
export class SideNavService {

    private sidebar$:BehaviorSubject<boolean> =  new BehaviorSubject<boolean>(true);

    public status():Observable<boolean>{
        return this.sidebar$.asObservable();
    }

    public setStatus(isOpen: boolean){
        this.sidebar$.next(isOpen);
    }

    public toggle(){
        this.sidebar$.next(!this.sidebar$.getValue());
    }

}
