import { Component, Input, OnDestroy, OnInit } from "@angular/core";
import { FormBuilder, FormControl, Validators } from "@angular/forms";
import { PlanUser } from "@app/core/model/plan/plan";
import { ChoiceWithIndices } from "ngx-mentions";
import { Observable, Subscription } from "rxjs";


@Component({
  selector: 'text-area-with-mentions',
  templateUrl: './text-area-with-mentions.component.html',
  styleUrls: ['./text-area-with-mentions.component.scss'],
})
export class TextAreaWithMentionsComponent implements OnInit, OnDestroy {
  USER_TRIGGER_CHARACTER: string = '@'

  @Input() form!: FormControl;
  @Input() planUsers: PlanUser[];
  @Input() clearCommentObservable: Observable<any>;
  @Input() threadId: string;

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
  searchRegexp = new RegExp('^([-&.\\w]+ *){0,3}$');

  constructor(
    private formBuilder: FormBuilder,
  ) {}
  
  ngOnInit(): void {
    this.internalForm = this.formBuilder.control('', Validators.required);
    this.internalFormSubscription = this.internalForm.valueChanges.subscribe(value => {
      this.form.setValue(this._formatTextWithSelections(value, this.mentions));
    });

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
        .substring(replaceContentIndex)
        .replace(selectionText, formattedText);
        formattedContent =
        replaceContentIndex === 0
          ? newReplace
          : formattedContent.substring(0, replaceContentIndex) + newReplace;

      replaceContentIndex = formattedContent.lastIndexOf('}}') + 2;
    });

    formattedContent = formattedContent.replace(/\n/g, '<br>');

    return formattedContent;
  }  
}
