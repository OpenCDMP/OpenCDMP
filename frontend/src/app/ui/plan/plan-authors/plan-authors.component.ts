import { CommonModule } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { PlanBlueprint, PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanUser } from '@app/core/model/plan/plan';
import { AuthService } from '@app/core/services/auth/auth.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { Guid } from '@common/types/guid';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-plan-authors',
  standalone: true,
  imports: [TranslateModule, CommonModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './plan-authors.component.html',
  styleUrl: './plan-authors.component.scss'
})
export class PlanAuthorsComponent {
    planUsers = input.required<PlanUser[]>();
    username = input.required<string>();
    planBlueprint = input<PlanBlueprint>(null);
    removeUser = input<boolean>(false);

    deleteAuthor = output<PlanUser>();

    planUserRoleEnum = PlanUserRole;

    constructor(
        protected enumUtils: EnumUtils,
        private authentication: AuthService,
    ){
    }

    protected isUserAuthor(userId: Guid): boolean {
		if (this.isAuthenticated()) {
			const principalId: Guid = this.authentication.userId();
			return this.username() && userId === principalId;
		} else return false;
	}

    protected isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

    protected removeUserFromPlan(author: PlanUser) {
        this.deleteAuthor.emit(author);
    }

    getSectionNameById(sectionId: Guid): string {
		if (sectionId == null || !this.planBlueprint()) return '';
		let sections: PlanBlueprintDefinitionSection[] = this.planBlueprint()?.definition?.sections?.filter((section: PlanBlueprintDefinitionSection) => sectionId === section.id);

		return sections == null ? '' : sections[0].label;
	}
}
