import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { AnnotationDialogComponent } from './annotation-dialog.component';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { TextAreaWithMentionsComponent } from '../components/text-area-with-mentions.component';
import { NgxMentionsModule } from 'ngx-mentions';
import { OverlayModule } from '@angular/cdk/overlay';
import { UserLogoPipe } from '@common/http/image/user-logo.pipe';
import { FallbackAvatarDirective } from '@common/http/image/fallback-avatar.directive';
import { RelativeTimeFormatPipe } from '@app/core/pipes/relative-time.pipe';

@NgModule({
    imports: [
        CommonUiModule,
        CommonFormsModule,
        FormattingModule,
        AutoCompleteModule,
        NgxMentionsModule,
        OverlayModule,
        UserLogoPipe,
        FallbackAvatarDirective,
        RelativeTimeFormatPipe,
        AnnotationDialogComponent,
        TextAreaWithMentionsComponent
    ],
    exports: [
        AnnotationDialogComponent,
        TextAreaWithMentionsComponent
    ],
})
export class AnnotationDialogModule { }
