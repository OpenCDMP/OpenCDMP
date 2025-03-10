import {CommonModule} from '@angular/common';
import { NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import { TableOfContentsInternal } from './table-of-contents-internal/table-of-contents-internal';
import { MatIconModule } from '@angular/material/icon';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { TableOfContentsComponent } from './table-of-contents.component';
import { TableOfContentsService } from './services/table-of-contents-service';

@NgModule({
    imports: [CommonModule, RouterModule, MatIconModule],
    declarations: [TableOfContentsComponent, TableOfContentsInternal],
    exports: [TableOfContentsComponent],
    providers: [VisibilityRulesService, TableOfContentsService]
})
export class TableOfContentsModule { }
