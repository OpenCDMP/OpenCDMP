import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { NavigationBreadcrumbComponent } from '../../../app/ui/misc/breadcrumb/navigation-breadcrumb.component';
import { TranslatePipe } from '@ngx-translate/core';

/**
 * Shared header for admin/management screens.
 *
 * Renders the breadcrumb, a real page title (h1) and an optional one-line
 * description that tells the user what the section governs, plus a slot for
 * primary actions. Designed to sit directly above an <app-admin-listing>.
 *
 * Usage:
 *   <app-admin-page-header
 *       titleKey="DESCRIPTION-TEMPLATE-LISTING.TITLE"
 *       descriptionKey="DESCRIPTION-TEMPLATE-LISTING.DESCRIPTION">
 *       <ng-container actions>
 *           <button mat-flat-button color="primary">…</button>
 *       </ng-container>
 *   </app-admin-page-header>
 */
@Component({
    selector: 'app-admin-page-header',
    templateUrl: './admin-page-header.component.html',
    styleUrls: ['./admin-page-header.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [NavigationBreadcrumbComponent, TranslatePipe]
})
export class AdminPageHeaderComponent {

    /** Translation key for the page title (rendered as an h1). */
    @Input() titleKey: string;

    /** Optional translation key for the one-line purpose description. */
    @Input() descriptionKey: string;

    /** Whether to render the navigation breadcrumb above the title. */
    @Input() showBreadcrumb = true;
}
