import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlanAuthorsEditorDialogComponent } from './plan-authors-editor-dialog.component';

describe('PlanAuthorsEditorDialogComponent', () => {
  let component: PlanAuthorsEditorDialogComponent;
  let fixture: ComponentFixture<PlanAuthorsEditorDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlanAuthorsEditorDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlanAuthorsEditorDialogComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
