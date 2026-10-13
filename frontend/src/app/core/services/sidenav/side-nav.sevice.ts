import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { BehaviorSubject } from "rxjs";

@Injectable({
    providedIn:'root'
})
export class SideNavService {

    private _open: boolean = true;
    private sidebar$:BehaviorSubject<boolean> =  new BehaviorSubject<boolean>(this._open);

    public isOpen(): boolean {
        return this._open;
    }

    public status():Observable<boolean>{
        return this.sidebar$.asObservable();
    }

    public setStatus(isOpen: boolean){
        this._open = isOpen;
        this.sidebar$.next(isOpen);
    }

    public toggle(){
        this._open = !this._open;
        this.sidebar$.next(this._open);
    }

}
