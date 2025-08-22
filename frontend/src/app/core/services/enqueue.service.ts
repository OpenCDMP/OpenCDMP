import { DestroyRef, inject, Injectable, signal } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { EMPTY, Observable, Subject } from "rxjs";
import { catchError, exhaustMap, finalize, switchMap, tap } from "rxjs/operators";

@Injectable({
    providedIn: 'root'
})
export class EnqueueService {

    #exhaustPipeline = new Subject<EnqueueRequest<any>>();
    #switchPipeLine = new Subject<EnqueueRequest<any>>();

    #exchaustChannelBusy = signal<boolean>(false);

    public exhaustPipelineBusy = this.#exchaustChannelBusy.asReadonly();

    constructor(private destroyRef: DestroyRef) {

        this.#exhaustPipeline.pipe(
            tap(() => this.#exchaustChannelBusy.set(true)),
            exhaustMap(
                payload => payload.payloadObservable.pipe(
                    tap(result => payload.notificationChannel.next(result)),
                    catchError((error) => {
                        payload.notificationChannel.error(error);
                        return EMPTY;
                    }),
                    finalize(
                        () => {
                            payload.notificationChannel.complete();
                            this.#exchaustChannelBusy.set(false);
                        }
                    )
                )
            ),

            takeUntilDestroyed(this.destroyRef)
        ).subscribe();

        this.#switchPipeLine.pipe(
            switchMap(
                payload => payload.payloadObservable.pipe(
                    tap(result => payload.notificationChannel.next(result)),
                    catchError((error) => {
                        payload.notificationChannel.error(error);
                        return EMPTY;
                    }),
                    finalize(() => payload.notificationChannel.complete())
                )
            ),
            takeUntilDestroyed(this.destroyRef)
        ).subscribe();
    }

    enqueueExhaustChannel<T>(payloadObservable: Observable<T>): Observable<T> {
        if (!payloadObservable) {
            return null;
        }

        const notificationChannel = new Subject<T>();

        //** In case the payload obvervable is synchronous,
        //* it will emit immediately the value in notification channel (Notification channel not returned to listener yet). As a result the listener will never get the result.
        //* set in pipeline asynchronously (microtask queue) and return channel first
        queueMicrotask(() => {
            this.#exhaustPipeline.next(
                {
                    notificationChannel,
                    payloadObservable
                }
            );
        })

        return notificationChannel.asObservable();
    }

    enqueueSwitchChannel<T>(payloadObservable: Observable<T>): Observable<T> {
        if (!payloadObservable) {
            return null;
        }

        const notificationChannel = new Subject<T>();

        //** In case the payload obvervable is synchronous,
        //* it will emit immediately the value in notification channel (Notification channel not returned to listener yet). As a result the listener will never get the result.
        //* set in pipeline asynchronously (microtask queue) and return channel first
        queueMicrotask(() => {
            this.#switchPipeLine.next(
                {
                    notificationChannel,
                    payloadObservable
                }
            );
        })

        return notificationChannel.asObservable();
    }

}

// enqueue tooltit here to use enqueue services in more specific cases (for example in services where we would like enqueing to be specific to service)
export function EnqueueToolkit(destroyRef = inject(DestroyRef)) {
    return new EnqueueService(destroyRef);
}



interface EnqueueRequest<T> {
    payloadObservable: Observable<T>;
    notificationChannel: Subject<T>;
}