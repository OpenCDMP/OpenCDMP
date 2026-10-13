import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { PlanBlueprintsPreviewComponent } from '../../../plan/plan-editor-blueprint/plan-blueprints-preview/plan-blueprints-preview.component';
import { TranslatePipe } from '@ngx-translate/core';

// Read-only, full-page view of a plan blueprint. Reuses PlanBlueprintsPreviewComponent in its
// readOnly mode (wizard chrome hidden, only the selected blueprint shown).
@Component({
    selector: 'app-plan-blueprint-library-view',
    templateUrl: './plan-blueprint-library-view.component.html',
    styleUrls: ['./plan-blueprint-library-view.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatButton, MatIcon, PlanBlueprintsPreviewComponent, TranslatePipe]
})
export class PlanBlueprintLibraryViewComponent extends BaseComponent implements OnInit {
    groupId: string = null;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private routerUtils: RouterUtilsService,
    ) {
        super();
    }

    ngOnInit(): void {
        this.groupId = this.route.snapshot.paramMap.get('id');
        if (!this.groupId) { this.back(); }
    }

    back() {
        this.router.navigate([this.routerUtils.generateUrl('/plan-blueprint-library')]);
    }
}
