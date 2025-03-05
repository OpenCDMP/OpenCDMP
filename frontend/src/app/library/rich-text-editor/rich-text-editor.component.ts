import { ChangeDetectionStrategy, Component, effect, EventEmitter, input, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, ViewChild } from "@angular/core";
import { FormControl } from "@angular/forms";
import { AngularEditorComponent, AngularEditorConfig } from "@kolkov/angular-editor";
import { Subscription } from "rxjs";

@Component({
    selector: 'rich-text-editor-component',
    template: `
		<div class="editor-wrapper" [class]="wrapperClasses()" [id]="id()">
			<angular-editor class="full-width editor" [class.disabled]="form()?.disabled" [class.error]="form()?.touched && form()?.invalid"
							[config]="editorConfig" [formControl]="form()" [required]="required()"
							placeholder="{{(placeholder()? (placeholder() | translate) : '') + (required() ? ' *': '')}}"
							(paste)="pasteWithoutFormatting($event)"></angular-editor>
			<mat-icon *ngIf="form()?.value && !form()?.disabled" (click)="form().patchValue('')" class="clear">close</mat-icon>
		</div>
	`,
    styleUrls: ['./rich-text-editor.component.scss'],
    // TODO: performance issue with this control. changed the change detection strategy in case it improves
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class RichTextEditorComponent implements OnDestroy {
    static nextId = 0;
    @ViewChild(AngularEditorComponent) editor: AngularEditorComponent

	form = input<FormControl>();
	id = input<string>(`rich-text-editor${RichTextEditorComponent.nextId++}`);
	placeholder = input<string>("Enter text");
	required = input<boolean>(false);
	wrapperClasses = input<string>("");

	formTouchEvent = input<EventEmitter<any>>();
	private formTouchSubscription: Subscription;

	editorConfig: AngularEditorConfig = {
		editable: !this.form()?.disabled,
		spellcheck: true,
		height: 'auto',
		minHeight: '0',
		maxHeight: 'auto',
		width: '100%',
		minWidth: '0',
		translate: 'yes',
		enableToolbar: true,
		showToolbar: true,
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
        effect(() => {
            const form = this.form();
            if(!form){ return; }
            if (this.formTouchEvent()) {
                this.observeFormStatus();
            }
            this.editorConfig.editable = !this.form()?.disabled;
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
}
