import { Component, Inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatDialogTitle, MatDialogContent } from '@angular/material/dialog';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { UserPlanBlueprintRole } from '@app/core/common/enum/user-plan-blueprint-role';
import { User, UserContactInfo } from '@app/core/model/user/user';
import { UserLookup } from '@app/core/query/user.lookup';
import { UserService } from '@app/core/services/user/user.service';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { Guid } from '@common/types/guid';
import { ContactInfoType } from '@notification-service/core/enum/contact-info-type.enum';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { PlanBlueprintEditorComponent } from '../plan-blueprint-editor.component';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { MatFormField, MatLabel, MatHint } from '@angular/material/form-field';
import { SingleAutoCompleteComponent } from '../../../../../library/auto-complete/single/single-auto-complete.component';
import { MatMenuTrigger, MatMenu, MatMenuContent, MatMenuItem } from '@angular/material/menu';
import { TranslatePipe } from '@ngx-translate/core';
import { Bp2CountPipe } from '../../../structure-editor/count.pipe';

export interface EditorsPanelData { editor: PlanBlueprintEditorComponent; }

/**
 * Side panel for the people who can change the blueprint, like the plan collaborators panel: people are found by
 * name or email, roles are explicit, removing someone asks first, and a saved blueprint's changes save immediately.
 */
@Component({
    selector: 'app-bp2-editors-panel',
    templateUrl: './editors-panel.component.html',
    styleUrls: ['./editors-panel.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatIconButton, MatIcon, CdkScrollable, MatDialogContent, MatFormField, MatLabel, SingleAutoCompleteComponent, FormsModule, ReactiveFormsModule, MatHint, MatButton, MatMenuTrigger, MatMenu, MatMenuContent, MatMenuItem, TranslatePipe, Bp2CountPipe]
})
export class EditorsPanelComponent implements OnInit {
	readonly Role = UserPlanBlueprintRole;
	search = new FormControl<User>(null);
	/** Email of each person shown, by user id. */
	emails = new Map<string, string>();
	readonly peopleConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: () => this.findPeople(null),
		filterFn: (like: string) => this.findPeople(like),
		getSelectedItem: (id: Guid) => this.userService.query(this.lookup(null, [id])).pipe(map(x => x.items[0])),
		displayFn: (item: User) => item.name,
		titleFn: (item: User) => item.name,
		subtitleFn: (item: User) => this.emailOf(item),
		valueAssign: (item: User) => item.id,
	};

	constructor(
		public dialogRef: MatDialogRef<EditorsPanelComponent>,
		@Inject(MAT_DIALOG_DATA) public data: EditorsPanelData,
		private userService: UserService,
		private dialog: MatDialog,
	) { }

	get editor(): PlanBlueprintEditorComponent { return this.data.editor; }

	ngOnInit(): void {
		const ids = this.editor.usersArray.controls.map(c => c.get('userId').value).filter(Boolean);
		if (ids.length) this.userService.query(this.lookup(null, ids)).subscribe({ next: result => result.items?.forEach(u => this.remember(u)), error: () => { } });
	}

	private lookup(like: string, ids?: Guid[]): UserLookup {
		const lookup = this.userService.buildAutocompleteLookup(like, this.editorIds(), ids);
		if (ids?.length) lookup.excludedIds = null;
		lookup.isActive = [IsActive.Active];
		lookup.project = { fields: [nameof<User>(x => x.id), nameof<User>(x => x.name), [nameof<User>(x => x.contacts), nameof<UserContactInfo>(x => x.type)].join('.'), [nameof<User>(x => x.contacts), nameof<UserContactInfo>(x => x.value)].join('.')] };
		return lookup;
	}

	/** People not on the blueprint yet, by name or email. */
	private findPeople(like: string): Observable<User[]> {
		return this.userService.query(this.lookup(like)).pipe(map(result => { result.items?.forEach(u => this.remember(u)); return result.items; }));
	}

	private editorIds(): Guid[] { return this.editor.usersArray.controls.map(c => c.get('userId').value).filter(Boolean); }
	private remember(user: User): void { const email = this.emailOf(user); if (user?.id && email) this.emails.set(user.id.toString(), email); }
	emailOf(user: User): string { return user?.contacts?.find(c => c.type === ContactInfoType.Email)?.value ?? ''; }
	email(userId: Guid): string { return this.emails.get(userId?.toString()) ?? ''; }

	add(user: User): void {
		if (!user) return;
		this.remember(user);
		this.editor.addUser(user, UserPlanBlueprintRole.Member);
		this.search.reset();
	}

	confirmRemove(index: number): void {
		const name = this.editor.userName(this.editor.usersArray.at(index)?.get('userId').value);
		this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '420px',
			data: {
				message: this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.EDITORS.REMOVE-CONFIRM', { name }),
				confirmButton: this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.EDITORS.REMOVE-FROM'),
				cancelButton: this.editor.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
				isDeleteConfirmation: true
			}
		}).afterClosed().subscribe(ok => { if (ok) this.editor.removeUser(index); });
	}

	initials(name: string): string { return (name ?? '').split(/\s+/).filter(Boolean).slice(0, 2).map(p => p[0].toUpperCase()).join('') || '?'; }
}
