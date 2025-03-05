import { CommonModule } from '@angular/common';
import { Component, computed, input, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatStepper, MatStepperModule } from '@angular/material/stepper';
import { TranslateModule } from '@ngx-translate/core';
import { MatButtonModule } from '@angular/material/button';
import { GENERAL_ANIMATIONS, STEPPER_ANIMATIONS } from '../animations/animations';

@Component({
  selector: 'app-custom-mat-stepper-header',
  imports: [CommonModule, MatStepperModule, MatIconModule, TranslateModule, MatButtonModule],
  animations: [...STEPPER_ANIMATIONS, ...GENERAL_ANIMATIONS],
  templateUrl: './custom-mat-stepper-header.component.html',
  styleUrl: './custom-mat-stepper-header.component.scss'
})
export class CustomMatStepperHeaderComponent {
    stepper = input<MatStepper>();
    linear = input<boolean>(true);

    steps = computed(() => this.stepper()?.steps);

    changeStep(index: number){
        if(!this.stepper()){return;}
        this.stepper().selectedIndex = index;
    }

    isStepUnlocked(stepIndex: number): boolean {
		if (!this.linear()) return true;

		if (stepIndex === 0) return true;
		if (stepIndex < 0) return false;
		//if previous step is valid then unlock
		let stepUnlocked: boolean = false;

		if (!this.isStepUnlocked(stepIndex - 1)) return false;

		this.stepper()?.steps.forEach((step, index) => {

			if (index + 1 == stepIndex) {//previous step

				if (step.completed) {
					stepUnlocked = true;
				}
			}
		});

		return stepUnlocked;
	}

    get progressStyle() {
		const diff = 3;

		return {
			'clip-path': `polygon(0 0, ${Math.round(this.barPercentage + diff)}% 0, ${Math.round(this.barPercentage)}% 100%, 0 100%)`
		}
	}

	get barPercentage() {
		if (!this.stepper || !this.stepper()?.steps) {
			return 0;
		}
		const selectedIndex = this.stepper()?.selectedIndex + 1;
		return (selectedIndex / this.stepper()?.steps.length) * 100;
	}

    isStepCompleted(stepIndex: number) {

		// let stepCompleted = false;
		// this.steps.forEach((step, index) => {
		// 	if (stepIndex === index) {
		// 		stepCompleted = step.completed;
		// 	}
		// });

		return  this.steps().toArray().at(stepIndex)?.completed;
	}

}
