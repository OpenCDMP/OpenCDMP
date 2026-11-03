import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, afterNextRender, computed, effect, inject, input, output, signal, viewChild } from '@angular/core';
import { DashboardSlideButtonType } from '@app/core/common/enum/dashboard-slide-button-type';
import { DashboardSlideImageFit } from '@app/core/common/enum/dashboard-slide-image-fit';
import { CommonUiModule } from '@common/ui/common-ui.module';

/** A content slide as members see it on the Dashboard; the slide editor shows it as its live preview. */
@Component({
	selector: 'app-dashboard-content-slide',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule],
	templateUrl: './dashboard-content-slide.component.html',
	styleUrl: './dashboard-content-slide.component.scss',
})
export class DashboardContentSlideComponent {
	readonly title = input<string>('');
	readonly text = input<string>('');
	readonly imageUrl = input<string | null>(null);
	readonly imageFit = input<DashboardSlideImageFit | null>(null);
	readonly buttonType = input<DashboardSlideButtonType | null>(null);
	readonly buttonTarget = input<string>('');
	readonly buttonText = input<string>('');
	/** Offer "Read more" when the text is cut; the editor's preview leaves it off. */
	readonly readMoreEnabled = input(false);
	readonly readMore = output<void>();

	private readonly textElement = viewChild<ElementRef<HTMLElement>>('textElement');
	protected readonly cut = signal(false);

	protected readonly fitImage = computed(() => this.imageFit() === DashboardSlideImageFit.Fit);
	protected readonly isEmail = computed(() => this.buttonType() === DashboardSlideButtonType.Email);
	protected readonly href = computed(() => this.isEmail() ? `mailto:${this.buttonTarget()}` : this.buttonTarget());
	protected readonly showButton = computed(() => this.buttonType() != null && !!this.buttonTarget() && !!this.buttonText());

	constructor() {
		const destroyRef = inject(DestroyRef);
		// a resize changes how much of the text fits; a new text may not change the clamped box's size
		afterNextRender(() => {
			const element = this.textElement()?.nativeElement;
			if (!element) return;
			const observer = new ResizeObserver(() => this.measure());
			observer.observe(element);
			destroyRef.onDestroy(() => observer.disconnect());
		});
		effect(() => {
			this.text();
			requestAnimationFrame(() => this.measure());
		});
	}

	private measure(): void {
		const element = this.textElement()?.nativeElement;
		if (element) this.cut.set(element.scrollHeight > element.clientHeight + 1);
	}
}
