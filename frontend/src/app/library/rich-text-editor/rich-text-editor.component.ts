import { ChangeDetectionStrategy, Component, effect, EventEmitter, input,  OnDestroy, ViewChild } from "@angular/core";
import { FormControl, FormsModule, ReactiveFormsModule } from "@angular/forms";
import { AngularEditorComponent, AngularEditorConfig, AngularEditorModule } from "@kolkov/angular-editor";
import { Subscription, takeUntil } from "rxjs";
import { BaseComponent } from "@common/base/base.component";
import { MatButton } from "@angular/material/button";
import { MatIcon } from "@angular/material/icon";
import { TranslatePipe } from "@ngx-translate/core";
@Component({
    selector: 'rich-text-editor-component',
    templateUrl: './rich-text-editor.component.html',
    styleUrls: ['./rich-text-editor.component.scss'],
    // TODO: performance issue with this control. changed the change detection strategy in case it improves
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [MatButton, MatIcon, AngularEditorModule, FormsModule, ReactiveFormsModule, TranslatePipe]
})
export class RichTextEditorComponent extends BaseComponent implements OnDestroy {
    static nextId = 0;
    @ViewChild(AngularEditorComponent) editor: AngularEditorComponent

	form = input<FormControl>();
	id = input<string>(`rich-text-editor${RichTextEditorComponent.nextId++}`);
	placeholder = input<string>("GENERAL.RICH-TEXT-EDITOR.PLACEHOLDER");
    label = input<string>();
	required = input<boolean>(false);
	/** Set to false when a heading outside the editor already shows the required asterisk. */
	showRequiredMarker = input<boolean>(true);
	wrapperClasses = input<string>("");
	includeAllConfig = input<boolean>(false);

	formTouchEvent = input<EventEmitter<any>>();
	private formTouchSubscription: Subscription;

	editorConfig: AngularEditorConfig = {
		editable: true,
		spellcheck: true,
		height: '5.625rem',
		minHeight: '0',
		maxHeight: 'auto',
		width: '100%',
		minWidth: '0',
		translate: 'yes',
		enableToolbar: true,
		showToolbar: false,
		placeholder: '',
		defaultParagraphSeparator: '',
		defaultFontName: '',
		defaultFontSize: '',
		sanitize: true,
		toolbarPosition: 'top',
		customClasses: [
			{ name: 'H1 header', class: '', tag: 'h1' },
			{ name: 'H2 header', class: '', tag: 'h2' },
			{ name: 'H3 header', class: '', tag: 'h3' },
			{ name: 'H4 header', class: '', tag: 'h4' },
			{ name: 'H5 header', class: '', tag: 'h5' },
			{ name: 'H6 header', class: '', tag: 'h6' },
			{ name: 'Highlight', class: '', tag: 'mark' }
		],
		toolbarHiddenButtons: [
			[
				'heading',
				'fontName'
			],
			[
				'fontSize',
				'backgroundColor',
				'insertImage',
				'insertVideo',
				'toggleEditorMode'
			],
			[
				'indent',
				'outdent'
			]
		]
	};


    constructor(){
        super();
        effect(() => {
            const form = this.form();
            if(!form){ return; }
            if (this.formTouchEvent()) {
                this.observeFormStatus();
            }
            this.editorConfig.editable = !this.form()?.disabled;
			if (this.includeAllConfig()) {
				this.editorConfig.toolbarHiddenButtons= [[], []]
			}
            this.form()?.statusChanges.pipe(takeUntil(this._destroyed)).subscribe((status) =>
                {
                    this.editorConfig.editable = status !== 'DISABLED';
                }
            )
        })
    }


	ngOnDestroy(): void {
		if (this.formTouchSubscription) {
			this.formTouchSubscription.unsubscribe();
		}
	}


	pasteWithoutFormatting($event) {
		$event.preventDefault();
		const text = $event.clipboardData.getData("text/plain");
		window.document.execCommand("insertText", false, text);
	}

    focus(){
        this.editor?.focus();
    }

	private observeFormStatus(): void {
        this.formTouchSubscription?.unsubscribe();
		this.formTouchSubscription = this.formTouchEvent()
			.subscribe(
				next => {
					if (next) {
						this.form().markAsTouched();
						this.form().updateValueAndValidity();
					}
				}
			);
	}

    toggleToolbar(){
        this.editorConfig.showToolbar = !this.editorConfig.showToolbar;
    }
}
