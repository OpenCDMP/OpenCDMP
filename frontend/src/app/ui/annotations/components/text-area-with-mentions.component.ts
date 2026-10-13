import { Component, Input, OnDestroy, OnInit, ChangeDetectionStrategy } from "@angular/core";
import { FormBuilder, FormControl, Validators, FormsModule, ReactiveFormsModule } from "@angular/forms";
import { PlanUser } from "@app/core/model/plan/plan";
import { ChoiceWithIndices, NgxMentionsModule, TextInputAutocompleteModule } from "ngx-mentions";
import { Observable, Subscription } from "rxjs";
import { MatFormField, MatLabel, MatError } from "@angular/material/form-field";
import { MatInput } from "@angular/material/input";
import { TextareaAutoresizeDirective } from "../../../../common/forms/textarea-autoresize-directive";
import { CdkOverlayOrigin, CdkConnectedOverlay } from "@angular/cdk/overlay";
import { TranslatePipe } from "@ngx-translate/core";


@Component({
    selector: 'text-area-with-mentions',
    templateUrl: './text-area-with-mentions.component.html',
    styleUrls: ['./text-area-with-mentions.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatFormField, MatLabel, MatInput, FormsModule, TextareaAutoresizeDirective, ReactiveFormsModule, NgxMentionsModule, MatError, CdkOverlayOrigin, CdkConnectedOverlay, TextInputAutocompleteModule, TranslatePipe]
})
export class TextAreaWithMentionsComponent implements OnInit, OnDestroy {
  USER_TRIGGER_CHARACTER: string = '@'

  @Input() form!: FormControl;
  @Input() planUsers: PlanUser[];
  @Input() clearCommentObservable: Observable<any>;
  @Input() threadId: string;
  @Input() placeholder: string;
  @Input() label: string;
  @Input() initialPayload: string;

  internalFormSubscription: Subscription;
  clearInputSubscription: Subscription;
  internalForm!: FormControl;
  choices: PlanUser[];
  loading: boolean = false;

  mentionsConfig = [
    {
      triggerCharacter: this.USER_TRIGGER_CHARACTER,
      getChoiceLabel: (item: PlanUser): string => {
        return `${this.USER_TRIGGER_CHARACTER}${item?.user?.name}`;
      },
    }
  ];
  mentions: ChoiceWithIndices[] = [];
  preselectedMentions: ChoiceWithIndices[] = [];
  searchRegexp = new RegExp('^([-&.\\w]+ *){0,3}$');

  constructor(
    private formBuilder: FormBuilder,
  ) {}
  
  ngOnInit(): void {
    this.internalForm = this.formBuilder.control('', Validators.required);
    this.internalFormSubscription = this.internalForm.valueChanges.subscribe(value => {
      this.form.setValue(this._formatTextWithSelections(value, this.mentions));
    });

    if (this.initialPayload) {
      const { text, mentions } = this._parsePayload(this.initialPayload);
      this.mentions = mentions;
      this.preselectedMentions = mentions;
      this.internalForm.setValue(text);
    }

    if (this.clearCommentObservable) {
      this.clearCommentObservable.subscribe(threadIdToClear => {
        if (threadIdToClear == this.threadId) {
          this.internalForm.reset();
          this.internalForm.markAsPristine();
          this.mentions = [];
        }
      });
    }
  }
  
  ngOnDestroy(): void {
    this.internalFormSubscription?.unsubscribe();
  }
  
  getDisplayLabel = (item: PlanUser): string => {
    return item?.user?.name;
  }

  loadChoices({searchText, triggerCharacter}
    : { searchText: string, triggerCharacter: string}
  ): void {

    if (triggerCharacter != this.USER_TRIGGER_CHARACTER) return;

    if (triggerCharacter == this.USER_TRIGGER_CHARACTER) {
    
      this.choices = this.planUsers?.filter(u => u?.user?.name.toLowerCase().indexOf(searchText.toLowerCase()) > -1);
    }
  }

  onSelectedChoicesChange(selections: ChoiceWithIndices[]): void {
    this.mentions = selections.map((choice: ChoiceWithIndices) => ({
      ...choice,
    }))

    this.internalForm.updateValueAndValidity();
  }

  onMenuShow(): void {}

  onMenuHide(): void {
    this.choices = undefined;
  }

  private _formatTextWithSelections(content: string, selections: ChoiceWithIndices[]): string {


    let formattedContent = content;
    let replaceContentIndex = 0;
    selections.forEach((selection: ChoiceWithIndices) => {
      
      const start = selection.indices.start;
      const end = selection.indices.end;
      
      const selectionText = content.substring(start, end);
      const formattedText = `@{{userid:${selection?.choice?.id}}}`;

      const newReplace = formattedContent
        ?.substring(replaceContentIndex)
        ?.replace(selectionText, formattedText);
        formattedContent =
        replaceContentIndex === 0
          ? newReplace
          : formattedContent?.substring(0, replaceContentIndex) + newReplace;

      replaceContentIndex = formattedContent?.lastIndexOf('}}') + 2;
    });

    formattedContent = formattedContent?.replace(/\n/g, '<br>');

    return formattedContent;
  }

  // The reverse of _formatTextWithSelections, so an edited comment keeps its mentions; unknown users stay as raw tokens.
  private _parsePayload(payload: string): { text: string, mentions: ChoiceWithIndices[] } {
    const source = payload.replace(/<br\s*\/?>/gi, '\n');
    const mentions: ChoiceWithIndices[] = [];
    let text = '';
    let last = 0;
    for (const match of source.matchAll(/@\{\{userid:([a-zA-Z0-9-]+)\}\}/g)) {
      text += source.substring(last, match.index);
      last = match.index + match[0].length;
      const planUser = this.planUsers?.find(u => u.id?.toString() === match[1]);
      if (!planUser) { text += match[0]; continue; }
      const label = this.mentionsConfig[0].getChoiceLabel(planUser);
      mentions.push({ choice: planUser, indices: { start: text.length, end: text.length + label.length, triggerCharacter: this.USER_TRIGGER_CHARACTER } });
      text += label;
    }
    return { text: text + source.substring(last), mentions };
  }
}
