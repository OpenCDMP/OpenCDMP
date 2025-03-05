import { computed, Injectable, Signal, signal } from '@angular/core';
import { toggleFontSize } from '@app/ui/misc/theme-helper';

@Injectable()
export class FontAccessibilityService {

    private _accessibleFontSignal = signal<boolean>(false);
    public accessibleFontSignal = this._accessibleFontSignal.asReadonly();

    constructor() {
        const isAccessibleFont = localStorage.getItem('largeText') === 'true';
        this._accessibleFontSignal.set(isAccessibleFont);
        if(isAccessibleFont){
            toggleFontSize(isAccessibleFont);
        }
    }

    public toggleFontSize(){
        this._accessibleFontSignal.update((value) => !value);
        localStorage.setItem('largeText', this._accessibleFontSignal() ? 'true' : 'false');
        toggleFontSize(this._accessibleFontSignal());
    }
}
