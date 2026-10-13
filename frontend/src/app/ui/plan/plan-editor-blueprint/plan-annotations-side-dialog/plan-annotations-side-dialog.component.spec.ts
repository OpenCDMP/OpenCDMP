import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlanAnnotationsSideDialogComponent } from './plan-annotations-side-dialog.component';

describe('PlanAnnotationsSideDialogComponent', () => {
  let component: PlanAnnotationsSideDialogComponent;
  let fixture: ComponentFixture<PlanAnnotationsSideDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlanAnnotationsSideDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlanAnnotationsSideDialogComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
