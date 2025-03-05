import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlanEvaluateDialogComponent } from './plan-evaluate-dialog.component';

describe('PlanEvaluateDialogComponent', () => {
  let component: PlanEvaluateDialogComponent;
  let fixture: ComponentFixture<PlanEvaluateDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlanEvaluateDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlanEvaluateDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
