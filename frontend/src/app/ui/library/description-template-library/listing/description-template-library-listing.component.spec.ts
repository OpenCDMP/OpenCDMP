import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DescriptionTemplateLibraryListingComponent } from './description-template-library-listing.component';

describe('DescriptionTemplateLibraryListingComponent', () => {
  let component: DescriptionTemplateLibraryListingComponent;
  let fixture: ComponentFixture<DescriptionTemplateLibraryListingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DescriptionTemplateLibraryListingComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DescriptionTemplateLibraryListingComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
