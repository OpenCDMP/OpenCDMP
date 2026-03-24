import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlanActiveUsersComponent } from './plan-active-users.component';

describe('PlanActiveUsersComponent', () => {
  let component: PlanActiveUsersComponent;
  let fixture: ComponentFixture<PlanActiveUsersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlanActiveUsersComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlanActiveUsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
