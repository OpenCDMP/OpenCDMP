
import { Component, Input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-toggle-show-text',
  imports: [TranslateModule],
  templateUrl: './toggle-show-text.component.html',
  styleUrl: './toggle-show-text.component.scss'
})
export class ToggleShowTextComponent {
    readonly MAX_WORDS = 10;
    @Input() text: string;
    @Input() innerHTML: string;
    @Input() set expanded(val: boolean){
        this.isExpanded = val ?? false;
    }
    get expanded(){
        return this.isExpanded;
    }
    @Input() showPreview: boolean = true;

    isExpanded: boolean = false;

    minimizeText(text: string): string {
        return text.split(/\s+/).slice(0, this.MAX_WORDS).join(' ') + '...'
    }
}
