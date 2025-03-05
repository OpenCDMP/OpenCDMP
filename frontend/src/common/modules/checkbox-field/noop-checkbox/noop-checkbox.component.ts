import { Component, Input } from "@angular/core";
import { FormControl } from "@angular/forms";
import { MAT_CHECKBOX_DEFAULT_OPTIONS } from "@angular/material/checkbox";
import { TooltipPosition } from "@angular/material/tooltip";

@Component({
    selector: 'app-noop-checkbox',
    templateUrl: 'noop-checkbox.component.html',
    styleUrls: ['noop-checkbox.component.scss'],
    providers: [
        {
            provide: MAT_CHECKBOX_DEFAULT_OPTIONS,
            useValue: { clickAction: 'noop' },
        }
    ],
    standalone: false
})
export class NoopCheckboxComponent {
  @Input() control: FormControl;
  @Input() checked: boolean;
  @Input() label: string;
  @Input() tooltipText: string | null = null;
  @Input() tooltipPosition: TooltipPosition = 'right';
}