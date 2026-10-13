import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlanBlueprintLibraryListingComponent } from './plan-blueprint-library-listing.component';

describe('PlanBlueprintLibraryListingComponent', () => {
  let component: PlanBlueprintLibraryListingComponent;
  let fixture: ComponentFixture<PlanBlueprintLibraryListingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlanBlueprintLibraryListingComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlanBlueprintLibraryListingComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
