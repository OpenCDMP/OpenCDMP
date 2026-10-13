import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InvitePlanUserComponent } from './invite-plan-user.component';

describe('InvitePlanUserComponent', () => {
  let component: InvitePlanUserComponent;
  let fixture: ComponentFixture<InvitePlanUserComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvitePlanUserComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InvitePlanUserComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
