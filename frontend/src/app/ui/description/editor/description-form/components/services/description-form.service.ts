import { Injectable } from "@angular/core";
import { Observable, Subject } from "rxjs";

@Injectable()
export class DescriptionFormService {

  public queryIncludesField: boolean;
  public queryIncludesAnnotation: boolean; 

  private detectChangesSubject: Subject<any> = new Subject<any>();
  
  public getDetectChangesObservable(): Observable<any> {
    return this.detectChangesSubject.asObservable();
  }

  public detectChanges(next: any): void {
    this.detectChangesSubject.next(next);
  }
  
  private scrollingToAnchorSubject: Subject<any> = new Subject<any>();
  
  public getScrollingToAnchorObservable(): Observable<any> {
    return this.detectChangesSubject.asObservable();
  }

  public scrollingToAnchor(next: any): void {
    this.detectChangesSubject.next(next);
  }
}
