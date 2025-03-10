import { CdkTableModule } from '@angular/cdk/table';
import { NgModule } from '@angular/core';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatOptionModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule, MatIconRegistry } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSliderModule } from '@angular/material/slider';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatStepperModule } from '@angular/material/stepper';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DomSanitizer } from '@angular/platform-browser';

@NgModule({
	imports: [
		MatToolbarModule,
		MatButtonModule,
		MatIconModule,
		MatCardModule,
		MatGridListModule,
		MatSnackBarModule,
		MatSidenavModule,
		MatListModule,
		MatChipsModule,
		MatFormFieldModule,
		MatSelectModule,
		MatOptionModule,
		MatInputModule,
		MatExpansionModule,
		MatAutocompleteModule,
		MatProgressSpinnerModule,
		MatProgressBarModule,
		MatTabsModule,
		MatDialogModule,
		MatMenuModule,
		MatRadioModule,
		MatStepperModule,
		MatTooltipModule,
		MatCheckboxModule,
		MatDatepickerModule,
		MatButtonToggleModule,
		MatSliderModule,
		MatSlideToggleModule,
		MatTableModule,
		MatPaginatorModule,
		CdkTableModule,
		MatSortModule,
		MatBadgeModule,
        MatDividerModule
	],
	exports: [
		MatToolbarModule,
		MatButtonModule,
		MatIconModule,
		MatCardModule,
		MatGridListModule,
		MatSnackBarModule,
		MatSidenavModule,
		MatListModule,
		MatChipsModule,
		MatFormFieldModule,
		MatSelectModule,
		MatOptionModule,
		MatInputModule,
		MatExpansionModule,
		MatAutocompleteModule,
		MatProgressSpinnerModule,
		MatProgressBarModule,
		MatTabsModule,
		MatDialogModule,
		MatMenuModule,
		MatRadioModule,
		MatStepperModule,
		MatTooltipModule,
		MatCheckboxModule,
		MatDatepickerModule,
		MatButtonToggleModule,
		MatSliderModule,
		MatSlideToggleModule,
		MatTableModule,
		MatPaginatorModule,
		CdkTableModule,
		MatSortModule,
		MatBadgeModule,
        MatDividerModule
	]
})
export class MaterialModule {
	constructor(iconRegistry: MatIconRegistry, private sanitizer: DomSanitizer){
		iconNames.forEach(iconName => {
			iconRegistry.addSvgIcon(iconName, this.sanitizer.bypassSecurityTrustResourceUrl(`/assets/icons/icon=${iconName}.svg`) )
		})
	}

}

const iconNames = [
	
];
