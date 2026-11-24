import { BreakpointObserver } from "@angular/cdk/layout";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { BehaviorSubject } from "rxjs";

@Injectable({
    providedIn:'root'
})
export class SideNavService {

    private _open: boolean = true;
    private _overlay: boolean = false;
    private sidebar$:BehaviorSubject<boolean> =  new BehaviorSubject<boolean>(this._open);

    constructor(breakpointObserver: BreakpointObserver) {
        // Same width at which the navbar swaps to its small-screen items (navbar.component.scss).
        breakpointObserver.observe('(max-width: 991px)').subscribe(state => {
            this._overlay = state.matches;
            this.setStatus(!state.matches);
        });
    }

    public isOpen(): boolean {
        return this._open;
    }

    public isOverlay(): boolean {
        return this._overlay;
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
