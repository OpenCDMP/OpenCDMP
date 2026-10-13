import { Pipe, PipeTransform, inject } from "@angular/core";
import moment, { Moment } from "moment";
import { DateTimeFormatPipe } from "./date-time-format.pipe";
import { CultureService } from "../services/culture/culture-service";

@Pipe({
	name: 'relativeTime',
    standalone: true
})
export class RelativeTimeFormatPipe implements PipeTransform {
    private dateFormatPipe =  inject(DateTimeFormatPipe)
    private cultureService = inject(CultureService);
    
    transform(dateTime: string | Moment, ...args:any[]): string {
        if(!dateTime){
            return null;
        }


        const culture = this.cultureService?.getCurrentCulture()?.name?.split('-')?.[0] ||  'en';

        // todo if from days longer then return different
        const days = moment().diff(moment(dateTime), 'days');
        
        if(days > 1){
            return this.dateFormatPipe.transform(dateTime, 'short')
        }
        return moment(dateTime).locale(culture).fromNow(days > 1);
    }
}