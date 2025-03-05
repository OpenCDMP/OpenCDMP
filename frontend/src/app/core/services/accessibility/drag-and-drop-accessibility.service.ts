import { Injectable, Signal, signal } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";

@Injectable()
export class DragAndDropAccessibilityService {
    private _reorderAssistiveText = signal<string>(null);
    private _reorderMode = signal<boolean>(false);

    public get assistiveTextSignal(): Signal<string> {
        return this._reorderAssistiveText;
    }

    public get reorderMode(): boolean {
        return this._reorderMode()
    }

    constructor(private language: TranslateService){

    }

    onKeyDown(params: {
        $event: KeyboardEvent, 
        currentIndex: number,
        listLength: number,
        itemName: string,
        moveUpFn: () => any,
        moveDownFn: () => any
    }) {
        const {$event, currentIndex, itemName, listLength, moveUpFn, moveDownFn} = params;
        switch($event.code){
            case 'ShiftLeft':
            case 'ShiftRight': {
                $event.preventDefault();
                this._reorderMode.update((reorderMode) => !reorderMode);
                this._reorderAssistiveText.set(this.language.instant('ALT-TEXT.REORDER-TABLE.REORDER-MODE-' + (this._reorderMode() ? 'ON' : 'OFF')));
                return;
            }
            case 'ArrowDown': {
                if(this._reorderMode()){
                    if(currentIndex + 1 < listLength){
                        moveDownFn();
                        this._reorderAssistiveText.set(this.language.instant('ALT-TEXT.REORDER-TABLE.ITEM-MOVED', {
                            item: itemName, 
                            index: currentIndex + 1,
                            length: listLength
                        }));
                    } else {
                        $event.stopPropagation();
                        this._reorderAssistiveText.set(this.language.instant('ALT-TEXT.REORDER-TABLE.ITEM-AT-END', {item: itemName}));
                    }
                }
                return;
            }
            case 'ArrowUp': {
                if(this._reorderMode()){
                    if(currentIndex - 1 >= 0){
                        moveUpFn();
                        this._reorderAssistiveText.set(this.language.instant('ALT-TEXT.REORDER-TABLE.ITEM-MOVED', {
                            item: itemName, 
                            index: currentIndex,
                            length: listLength
                        }));
                    } else {
                        $event.stopPropagation();
                        this._reorderAssistiveText.set(this.language.instant('ALT-TEXT.REORDER-TABLE.ITEM-AT-TOP', {item: itemName}));
                    }
                }
                return;
            }
            default: {
                return;
            }
        }
    }
}