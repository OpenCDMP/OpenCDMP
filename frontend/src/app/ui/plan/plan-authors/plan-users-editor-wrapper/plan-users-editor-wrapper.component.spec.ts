import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlanUsersEditorWrapperComponent } from './plan-users-editor-wrapper.component';

describe('PlanUsersEditorWrapperComponent', () => {
  let component: PlanUsersEditorWrapperComponent;
  let fixture: ComponentFixture<PlanUsersEditorWrapperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlanUsersEditorWrapperComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlanUsersEditorWrapperComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
