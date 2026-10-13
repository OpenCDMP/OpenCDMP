import { NgModule } from '@angular/core';
import { ArrayToStringPipe } from '@common/formatting/pipes/array-to-string.pipe';
import { DataTableDateOnlyFormatPipe, DateOnlyPipe } from '@common/formatting/pipes/date-only-format.pipe';
import { IsActiveTypePipe } from './pipes/is-active-type.pipe';
import { NotificationTypePipe } from './pipes/notification-type.pipe';
import { ReferenceSourceTypePipe } from './pipes/reference-source-type.pipe';
import { LockTargetTypePipe } from './pipes/lock-target-type.pipe';
import { UsageLimitTargetMetricPipe } from './pipes/usage-limits-target-metric.pipe';
import { InvokeMemoizedPipe } from './pipes/invoke-memoized.pipe';

//
//
// This is shared module that provides all formatting utils. Its imported only once on the AppModule.
//
//
@NgModule({
    imports: [InvokeMemoizedPipe, ArrayToStringPipe,
        DateOnlyPipe,
        DataTableDateOnlyFormatPipe,
        IsActiveTypePipe,
        ReferenceSourceTypePipe,
        NotificationTypePipe,
        LockTargetTypePipe,
        UsageLimitTargetMetricPipe],
    exports: [
        ArrayToStringPipe,
        DateOnlyPipe,
        DataTableDateOnlyFormatPipe,
        IsActiveTypePipe,
        ReferenceSourceTypePipe,
        NotificationTypePipe,
        LockTargetTypePipe,
        UsageLimitTargetMetricPipe,
        InvokeMemoizedPipe
    ],
    providers: [
        ArrayToStringPipe,
        DateOnlyPipe,
        DataTableDateOnlyFormatPipe,
        IsActiveTypePipe,
        ReferenceSourceTypePipe,
        NotificationTypePipe,
        LockTargetTypePipe,
        UsageLimitTargetMetricPipe,
        InvokeMemoizedPipe
    ]
})
export class CommonFormattingModule { }
