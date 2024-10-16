import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from "@angular/core";
import { FormControl } from "@angular/forms";
import { AngularEditorConfig } from "@kolkov/angular-editor";
import { Subscription } from "rxjs";

@Component({
	selector: 'rich-text-editor-component',
	template: `
		<div class="editor-wrapper" [class]="wrapperClasses">
			<angular-editor class="full-width editor" [ngClass]="editable ? '': 'disabled'" [id]="id"
							[config]="editorConfig" [formControl]="form" [required]="required"
							placeholder="{{(placeholder? (placeholder | translate) : '') + (required ? ' *': '')}}"
							(paste)="pasteWithoutFormatting($event)"></angular-editor>
			<mat-icon *ngIf="form.value && editable" (click)="parentFormGroup.get(controlName).patchValue('')" class="clear">close</mat-icon>
		</div>
	`,
	styleUrls: ['./rich-text-editor.component.scss'],
	// TODO: performance issue with this control. changed the change detection strategy in case it improves
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class RichTextEditorComponent implements OnInit, OnChanges, OnDestroy {

	@Input() form: FormControl;
	@Input() id: string = "editor1";
	@Input() placeholder: string = "Enter text";
	@Input() required: boolean = false;
	@Input() wrapperClasses: string = "";
	@Input() editable: boolean = true;

	@Input() formTouchEvent: EventEmitter<any>;
	private formTouchSubscription: Subscription;

	editorConfig: AngularEditorConfig = {
		editable: this.editable,
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


	ngOnInit(): void {
		if (this.formTouchEvent) {
			this.observeFormStatus();
		}
	}

	ngOnChanges(changes: SimpleChanges): void {
		if (changes['editable']) {
			this.editorConfig.editable = this.editable;
		}
	}

	ngOnDestroy(): void {
		if (this.formTouchSubscription) {
			this.formTouchSubscription.unsubscribe();
		}
	}

	ngAfterContentInit() {
		this.editorConfig.editable = this.editable;
	}

	pasteWithoutFormatting($event) {
		$event.preventDefault();
		const text = $event.clipboardData.getData("text/plain");
		window.document.execCommand("insertText", false, text);
	}

	private observeFormStatus(): void {
		this.formTouchSubscription = this.formTouchEvent
			.subscribe(
				next => {
					if (next) {
						this.form.markAsTouched();
						this.form.updateValueAndValidity();
					}
				}
			);
	}
}
