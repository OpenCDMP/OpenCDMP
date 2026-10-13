import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { DescriptionTemplatePersist } from '@app/core/model/description-template/description-template-persist';
import { DescriptionTemplateEditorComponent } from '../../description-template-editor.component';
import { MatSlideToggle } from '@angular/material/slide-toggle';
import { FormsModule } from '@angular/forms';
import { MatIcon } from '@angular/material/icon';
import { MatButtonToggleGroup, MatButtonToggle } from '@angular/material/button-toggle';
import { PreviewMappingsDirective } from '../../../../structure-editor/review/preview-mappings.directive';
import { MatButton } from '@angular/material/button';
import { FinalPreviewComponent } from '../../components/final-preview/final-preview.component';
import { DownloadOutlineComponent } from '../../../../structure-editor/review/download-outline/download-outline.component';
import { PluginReviewComponent } from '../../../../structure-editor/review/plugin-review/plugin-review.component';
import { SemanticsCardComponent } from '../../../../structure-editor/review/semantics-card/semantics-card.component';
import { TranslatePipe } from '@ngx-translate/core';
import { Bp2CountPipe } from '../../../../structure-editor/count.pipe';

/**
 * Review tab: the template as an author fills it in (the final preview), and what a download contains.
 * The readiness, semantics, counts and download outline are shared with the plan blueprint editor.
 */
@Component({
    selector: 'app-dt2-review-tab',
    templateUrl: './review-tab.component.html',
    styleUrls: ['./review-tab.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatSlideToggle, FormsModule, MatIcon, MatButtonToggleGroup, MatButtonToggle, PreviewMappingsDirective, MatButton, FinalPreviewComponent, DownloadOutlineComponent, PluginReviewComponent, SemanticsCardComponent, TranslatePipe, Bp2CountPipe]
})
export class Dt2ReviewTabComponent implements OnInit {
	/** Taken once when the tab opens, so the preview does not rebuild on every change detection. */
	persist: DescriptionTemplatePersist;
	/** Which author moment the preview shows: filling the asset, or the download. */
	mode: 'fill' | 'download' = 'fill';
	/** Shows each input's keys in the author preview. */
	showMappings = false;

	constructor(public editor: DescriptionTemplateEditorComponent) { }

	ngOnInit(): void { this.persist = this.editor.previewPersist(); }

	get hasSections(): boolean { return (this.persist?.definition?.sections?.length ?? 0) > 0; }
}
