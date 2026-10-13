import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExistingPlanUsersEditorComponent } from './existing-plan-users-editor.component';

describe('ExistingPlanUsersEditorComponent', () => {
  let component: ExistingPlanUsersEditorComponent;
  let fixture: ComponentFixture<ExistingPlanUsersEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExistingPlanUsersEditorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExistingPlanUsersEditorComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
