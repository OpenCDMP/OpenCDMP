import {CommonModule} from '@angular/common';
import { NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import { TableOfContentsInternal } from './table-of-contents-internal/table-of-contents-internal';
import { MatIconModule } from '@angular/material/icon';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { TableOfContentsComponent } from './table-of-contents.component';
import {TranslateModule} from "@ngx-translate/core";
import {MatBadgeModule} from "@angular/material/badge";
import { MatExpansionModule } from '@angular/material/expansion';

@NgModule({
    imports: [CommonModule, RouterModule, MatIconModule, MatBadgeModule, TranslateModule, MatExpansionModule, TableOfContentsComponent, TableOfContentsInternal],
    exports: [TableOfContentsComponent, TableOfContentsInternal],
    providers: [VisibilityRulesService]
})
export class TableOfContentsModule { }
