import { Injectable, signal, OnDestroy } from '@angular/core';
import { Plan } from '@app/core/model/plan/plan';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { BaseService } from '@common/base/base.service';
import { Guid } from '@common/types/guid';
import { Subscription, takeUntil, take, interval, catchError, throwError, map, Observable, forkJoin, tap, of, defaultIfEmpty } from 'rxjs';
import { PopupNotificationDialogComponent } from '@app/library/notification/popup/popup-notification.component';
import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { AuthService } from '@app/core/services/auth/auth.service';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { PlanTempStorageService } from './plan-temp-storage.service';
import { UntypedFormGroup } from '@angular/forms';
import { Lock } from '@app/core/model/lock/lock.model';

@Injectable()
export class PlanLockService extends BaseService implements OnDestroy{
    lockInterval = this.configurationService.lockInterval;
    plan: Plan;
    private _editLockIds = signal<string[]>([]);

    private _externalLockIds = signal<string[]>([]);

    private _transientLockId = signal<string>(null);

    // Tracks the most recently requested lock id so that in-flight checkAndLock
    // responses for a selection that has since changed can be discarded/released
    // instead of leaking an orphaned server lock.
    private _pendingLockId: string = null;

    private _initializeVersion = signal<number>(0);
    public initializeVersion = this._initializeVersion.asReadonly();
    
    private lockSubscriptionMap = new Map<string, Subscription>([]);

    public editLockIds = this._editLockIds.asReadonly();
    public lockOnEdit(id: Guid, targetType: LockTargetType) {
        const idStr = id?.toString();
        if(this._externalLockIds()?.includes(idStr) || this._transientLockId() !== idStr){ return; }
        this._editLockIds.update((curr) => curr.includes(idStr) ? curr : [...curr, idStr]);
    }
    public discardEdit(id: Guid){
        this._editLockIds.update((curr) => curr.filter((x) => x !== id?.toString()));
        if(this._transientLockId() !== id?.toString()){
            this.unlock(id).pipe(takeUntil(this._destroyed)).subscribe();
        }
    }

    public externalLockIds = this._externalLockIds.asReadonly();

    public transientLockId = this._transientLockId.asReadonly();

    // FE-1: form group per actively-held lock id, so a lock lost later (failed heartbeat) or an
    // acquisition error can re-disable the very form the user is editing — instead of silently
    // leaving it editable with no backing server lock.
    private lockedFormGroups = new Map<string, UntypedFormGroup>();

    // FE-1: ids whose lock was held by THIS user and then lost/failed mid-session. Distinct from
    // externalLockIds (locked by someone else, never mine) so the UI can say "you lost the lock"
    // rather than just "locked".
    private _lostLockIds = signal<string[]>([]);
    public lostLockIds = this._lostLockIds.asReadonly();

    // FE-1: provenance for locked ids (who holds it / since when) for "locked by {name}" UI.
    private _lockOwners = signal<Map<string, LockOwnerInfo>>(new Map());
    public lockOwners = this._lockOwners.asReadonly();


    constructor(
        private configurationService: ConfigurationService,
        private lockService: LockService,
        private dialog: MatDialog,
        private language: TranslateService,
        private authService: AuthService,
        private planTempStorage: PlanTempStorageService
    ){
        super();
    }

    public initialize(plan: Plan): Observable<boolean[]>{
        if(!plan?.id){
            this._initializeVersion.update(v => v + 1);
            return of([]);
        }
        const descriptionChecks = (plan.descriptions ?? [])
            .filter((description) => !!this.planTempStorage.descriptions()?.get(description.id.toString())?.formGroup)
            .map((description) => this.checkLockStatus(description.id, LockTargetType.Description));
        return forkJoin([
            this.checkLockStatus(plan.id, LockTargetType.Plan),
            ...descriptionChecks
        ]).pipe(tap(() => this._initializeVersion.update(v => v + 1)))
    }

    public checkLockStatus(id: Guid, targetType: LockTargetType, showDialog: boolean = false): Observable<boolean>{
        return this.lockService.checkLockStatus(id).pipe(
            takeUntil(this._destroyed),
            catchError((error) => throwError(() => error)),
            map((lockStatus) => {
                const isLocked = lockStatus.status;
                if (isLocked) {
                    this._externalLockIds.update((ids) => ids.includes(id?.toString()) ? ids : [...ids, id?.toString()]);
                    // FE-1: capture who holds the lock and since when, for "locked by {name}" UI.
                    this.recordOwner(id?.toString(), lockStatus.lock);
                    if(showDialog){
                        this.openLockedDialog(targetType)
                    }
                }
                return lockStatus.status;
            })
        );
    }
    
    public checkAndLock(
        targetId: Guid,
        targetType: LockTargetType,
        formGroup: UntypedFormGroup,
        showDialog: boolean = false
    ) {
        const id = targetId.toString();
        if(!this.authService.currentAccountIsAuthenticated() || this._transientLockId() == id?.toString()){ return; }
        this._pendingLockId = id;

        const removePrevTransientLock = this._transientLockId() && !this.editLockIds()?.includes(this._transientLockId())
        const preProcess =  removePrevTransientLock ? this.unlock(Guid.parse(this._transientLockId())).pipe(takeUntil(this._destroyed), catchError(() => of(null))) : of(null);
        preProcess.subscribe(() => {
            //already locked, already edited
            if(this._editLockIds().includes(id) && this.lockSubscriptionMap.has(id)){
                this._transientLockId.set(id);
                return;
            }
            
            this.lockService.checkAndLock(targetId, targetType).pipe(takeUntil(this._destroyed))
            .subscribe({
                next: (lockedByThisUser) => {
                    if(lockedByThisUser){
                        if(this._pendingLockId !== id){
                            // The selection moved on while this request was in flight; the
                            // server lock we just acquired is now orphaned. Release it instead
                            // of tracking it, otherwise it would stay locked (and kept alive by
                            // a touchLock interval) until reset()/destroy, blocking other users.
                            this.lockService.unlockTarget(targetId)
                                .pipe(takeUntil(this._destroyed), catchError(() => of(null))).subscribe();
                            return;
                        }
                        formGroup.enable({onlySelf: true});
                        this._transientLockId.set(id);
                        // FE-1: remember the form so a later lost lock can re-disable it, and clear any
                        // prior lost/owner state for this id now that we (re)hold it.
                        this.lockedFormGroups.set(id, formGroup);
                        this.clearLostAndOwner(id);
                        if(this.lockSubscriptionMap.has(id)){
                            this.lockSubscriptionMap.get(id).unsubscribe();
                        }
                        const touchLock$ = interval(this.configurationService.lockInterval)
                            .pipe(takeUntil(this._destroyed)).subscribe(() => {
                                this.touchLock(targetId, targetType);
                            });

                        this.lockSubscriptionMap.set(id, touchLock$);
                        this._externalLockIds.update((curr) => curr.filter(x => x !== id));
                        
                        if(!this._editLockIds().includes(id)){
                            this.lockOnFormChange(formGroup, targetId, targetType);
                        }
                    } else {
                        //locked by somebody else
                        this.deleteLock(targetId);
                        this._externalLockIds.update((curr) => [...curr, id]);
                        formGroup.disable({onlySelf: true});
                        if(showDialog){
                            this.openLockedDialog(targetType)
                        }
                    }
                },
                error: (error) => {
                    // FE-1: previously this was a known gap — on an acquisition failure we left the
                    // form enabled with no backing server lock, so edits could be made that would not
                    // be saveable. Now we disable the form, flag the id as lost, and notify, so the
                    // user is never silently editing changes that can't be persisted.
                    this.deleteLock(targetId);
                    formGroup.disable({onlySelf: true});
                    this.markLost(id);
                    this.openLockedDialog(targetType);
                }
            })
        })
    }

    public openLockedDialog(targetType: LockTargetType){
        this.dialog.open(PopupNotificationDialogComponent, {
            data: {
                title: this.language.instant(targetType == LockTargetType.Plan ? 'PLAN-EDITOR.LOCKED-DIALOG.TITLE' : 'DESCRIPTION-EDITOR.LOCKED-DIALOG.TITLE'),
                message: this.language.instant(targetType == LockTargetType.Plan ? 'PLAN-EDITOR.LOCKED-DIALOG.MESSAGE' : 'DESCRIPTION-EDITOR.LOCKED-DIALOG.MESSAGE')
            }, maxWidth: '30em'
        });
    }


    public unlockTransient(): Observable<void>{
        if(!this.transientLockId() || this.editLockIds()?.includes(this.transientLockId())){
            return of(null);
        }
        return this.unlock(Guid.parse(this.transientLockId()));
    }

    public unlock(id: Guid): Observable<any>{
        if(this.lockSubscriptionMap.has(id.toString())){
            // untrack before the request: reset() and unlockTransient() can run in the same tick, and two DELETEs for one lock fail with a 500
            this.deleteLock(id);
            return this.lockService.unlockTarget(id).pipe(
                catchError((error) => of(null))
            );
        }
        return of(null);
    }
    
    private touchLock(targetId: Guid, targetType: LockTargetType){
        this.lockService.touchLock(targetId).pipe(takeUntil(this._destroyed)).subscribe({
            error: (err) => {
                this.checkLockStatus(targetId, targetType, false)
                .pipe(takeUntil(this._destroyed))
                .subscribe((isLocked) => {
                    if(isLocked){ // genuinely lost to someone else
                        const idStr = targetId.toString();
                        const form = this.lockedFormGroups.get(idStr);
                        this.deleteLock(targetId);
                        this._externalLockIds.update((curr) => curr.includes(idStr) ? curr : [idStr, ...curr])
                        // FE-1: the lock was held by this user and has now been lost mid-edit. Re-disable
                        // the form and tell the user instead of silently leaving it editable (which would
                        // let them keep making changes that the next save would fail to persist / could
                        // clobber the new lock holder). Owner provenance was captured by checkLockStatus above.
                        form?.disable({onlySelf: true});
                        this.markLost(idStr);
                        this.openLockedDialog(targetType);
                    }
                });
                // else: transient blip — keep the lock; next interval tick re-touches
            }
        });
    }

    private deleteLock(targetId: Guid){
        const id = targetId.toString();
        this.lockSubscriptionMap.get(id)?.unsubscribe();
        this.lockSubscriptionMap.delete(id);
        // FE-1: drop the stored form reference too. Lost/owner state is intentionally NOT cleared here
        // (it is a terminal UI state kept until the lock is re-acquired or the editor resets).
        this.lockedFormGroups.delete(id);
        this._editLockIds.update((curr) => curr.filter(x => x !== id));
        if(this._transientLockId() == id){ this._transientLockId.set(null) }
    }

    // FE-1 helpers ----------------------------------------------------------
    private markLost(id: string){
        if(!id){ return; }
        this._lostLockIds.update((curr) => curr.includes(id) ? curr : [...curr, id]);
    }

    private clearLostAndOwner(id: string){
        if(!id){ return; }
        this._lostLockIds.update((curr) => curr.includes(id) ? curr.filter(x => x !== id) : curr);
        this._externalLockIds.update((curr) => curr.includes(id) ? curr.filter(x => x !== id) : curr);
        this._lockOwners.update((m) => {
            if(!m.has(id)){ return m; }
            const next = new Map(m);
            next.delete(id);
            return next;
        });
    }

    private recordOwner(id: string, lock: Lock){
        if(!id || !lock){ return; }
        this._lockOwners.update((m) => {
            const next = new Map(m);
            next.set(id, { name: lock.lockedBy?.name, at: lock.lockedAt });
            return next;
        });
    }

    formChange$: Subscription;
    public lockOnFormChange(formGroup: UntypedFormGroup, targetId: Guid, targetType: LockTargetType){
        this.formChange$?.unsubscribe();
        //already edited and locked
        if(this._editLockIds().includes(targetId.toString())){ return; }
        this.formChange$ = formGroup.valueChanges
        .pipe(take(1)).subscribe((change) => {
            this.lockOnEdit(targetId, targetType)
        })
    }

    reset(): Observable<any>{
        this._editLockIds.set([]);
        this._externalLockIds.set([]);
        this._transientLockId.set(null);
        this._pendingLockId = null;
        this._initializeVersion.set(0);
        // FE-1: clear lost/owner tracking and stored form refs on reset.
        this._lostLockIds.set([]);
        this._lockOwners.set(new Map());
        this.lockedFormGroups = new Map();

        this.formChange$?.unsubscribe();

        const unlockObservables: Observable<any>[] = [];
        this.lockSubscriptionMap?.forEach((sub, key) => {
            if(key){
                unlockObservables.push(this.unlock(Guid.parse(key)));
            }
        })
        // defaultIfEmpty guards the no-locks-held case: forkJoin([]) completes without ever
        // emitting `next`, which would silently skip subscribers that act in their next handler
        // (e.g. closeSafe's router.navigate after saveAndClose).
        return forkJoin(unlockObservables).pipe(defaultIfEmpty(null), tap(() => this.lockSubscriptionMap = new Map([])));
    }

    ngOnDestroy(){
        this.reset().subscribe();
        super.ngOnDestroy();
    }
}

// FE-1: who holds an external/lost lock and since when, for "locked by {name}" UI.
export interface LockOwnerInfo {
    name?: string;
    at?: Date;
}

