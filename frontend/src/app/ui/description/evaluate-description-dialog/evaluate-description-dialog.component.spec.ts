import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluateDescriptionDialogComponent } from './evaluate-description-dialog.component';

describe('EvaluateDescriptionDialogComponent', () => {
  let component: EvaluateDescriptionDialogComponent;
  let fixture: ComponentFixture<EvaluateDescriptionDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluateDescriptionDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluateDescriptionDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
