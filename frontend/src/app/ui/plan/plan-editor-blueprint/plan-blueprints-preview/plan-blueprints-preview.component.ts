import {Component, EventEmitter, inject, Input, Output, QueryList, ViewChildren} from "@angular/core";
import {
	DescriptionTemplatesInSection,
	FieldInSection,
	PlanBlueprint,
	PlanBlueprintDefinition,
	PlanBlueprintDefinitionSection,
    ReferenceTypeFieldInSection,
    SystemFieldInSection
} from "@app/core/model/plan-blueprint/plan-blueprint";
import {PlanBlueprintService} from "@app/core/services/plan/plan-blueprint.service";
import {QueryResult} from "@common/model/query-result";
import {catchError, map, switchMap, takeUntil, tap} from "rxjs/operators";
import {BaseComponent} from "@common/base/base.component";
import {PlanBlueprintLookup} from "@app/core/query/plan-blueprint.lookup";
import {IsActive} from "@app/core/common/enum/is-active.enum";
import {nameof} from "ts-simple-nameof";
import {
	DescriptionTemplate,
	DescriptionTemplateDefinition,
    DescriptionTemplateFieldSet,
	DescriptionTemplatePage,
	DescriptionTemplateSection
} from "@app/core/model/description-template/description-template";
import {DescriptionTemplateService} from "@app/core/services/description-template/description-template.service";
import {DescriptionTemplateLookup} from "@app/core/query/description-template.lookup";
import {PlanBlueprintStatus} from "@app/core/common/enum/plan-blueprint-status";
import {PlanBlueprintVersionStatus} from "@app/core/common/enum/plan-blueprint-version-status";
import {DescriptionTemplateVersionStatus} from "@app/core/common/enum/description-template-version-status";
import {DescriptionTemplateStatus} from "@app/core/common/enum/description-template-status";
import JsPDF from "jspdf";
import html2canvas from 'html2canvas';
import {TenantHandlingService} from "@app/core/services/tenant/tenant-handling.service";
import {Guid} from "@common/types/guid";
import {HttpErrorHandlingService} from "@common/modules/errors/error-handling/http-error-handling.service";
import {MatDialog} from "@angular/material/dialog";
import {DescriptionTemplatesDialogComponent} from "@app/ui/plan/plan-editor-blueprint/description-templates-dialog/description-templates-dialog.component";
import { TenantConfiguration } from "@app/core/model/tenant-configuaration/tenant-configuration";
import { combineLatest, Observable, of } from "rxjs";
import { PlanBlueprintFieldCategory } from "@app/core/common/enum/plan-blueprint-field-category";
import { EnumUtils } from "@app/core/services/utilities/enum-utils.service";
import { ReferenceType } from "@app/core/model/reference-type/reference-type";
import {MatExpansionPanel} from "@angular/material/expansion";
import { PlanEditorEntityResolver } from "../resolvers/plan-editor-enitity.resolver";

@Component({
	selector: 'app-plan-blueprints-preview-component',
	templateUrl: './plan-blueprints-preview.component.html',
	styleUrls: ['./plan-blueprints-preview.component.scss'],
	standalone: false

})

export class PlanBlueprintsPreviewComponent extends BaseComponent {
    @ViewChildren('baseExpansionPanel') baseExpansionPanels: QueryList<MatExpansionPanel>;
    @ViewChildren('nestedExpansionPanel') nestedExpansionPanels: QueryList<MatExpansionPanel>;
	public lookup: PlanBlueprintLookup;
	public planBlueprints: PlanBlueprint[];
	public planBlueprintCount = 0;
	pageSize: number = 5;
	page: number = 1;
	searchTerm = "";
	public selectedBlueprint: PlanBlueprint = null; // selected blueprint
	@Input() public selectedBlueprintId = null; // selected blueprint
	public defaultBlueprint: PlanBlueprint = null;
	public descriptionTemplates = new Map();
	public sectionIsOpen = [];
	@Output() proceedWithBlueprint: EventEmitter<PlanBlueprint> = new EventEmitter();
	img = "";
	exportingPDF = false;
	dialog = inject(MatDialog);

    planBlueprintSectionFieldCategoryEnum = PlanBlueprintFieldCategory;

	openDialog() {
		this.dialog.open(DescriptionTemplatesDialogComponent, {
			width: '80vw',
			height: '80vh',
			maxWidth: 'none', // Remove default max-width
			maxHeight: 'none', // Remove default max-height
		});
	}
	private readonly lookupFields: string[] = [
        ...PlanEditorEntityResolver.blueprintLookupFields(),
		nameof<PlanBlueprint>(x => x.id),
		nameof<PlanBlueprint>(x => x.label),
		nameof<PlanBlueprint>(x => x.description),
		nameof<PlanBlueprint>(x => x.code),
		nameof<PlanBlueprint>(x => x.groupId),
		[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<ReferenceTypeFieldInSection>(x => x.referenceType), nameof<ReferenceType>(x => x.name)].join('.'),
		[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.hasTemplates)].join('.'),
	];

	private readonly descriptionTemplateLookupFields: string[] = [
		nameof<DescriptionTemplate>(x => x.id),
		nameof<DescriptionTemplate>(x => x.label),
		nameof<DescriptionTemplate>(x => x.code),
		nameof<DescriptionTemplate>(x => x.description),
		nameof<DescriptionTemplate>(x => x.version),
		nameof<DescriptionTemplate>(x => x.groupId),
		[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.id)].join('.'),
		[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.ordinal)].join('.'),
		[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.title)].join('.'),
		[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.id)].join('.'),
		[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.ordinal)].join('.'),
		[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.title)].join('.'),
		[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.description)].join('.'),
		[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.sections)].join('.'),
		[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.id)].join('.'),
		[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.ordinal)].join('.'),
		[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.title)].join('.'),
		[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets)].join('.'),

	];

	constructor(
        private planBlueprintService: PlanBlueprintService,
        private descriptionTemplateService: DescriptionTemplateService,
        private tenantHandlingService: TenantHandlingService,
        protected httpErrorHandlingService: HttpErrorHandlingService,
        protected enumUtils: EnumUtils
    ) {
		super();
	}

    ngOnInit(){
        combineLatest([
            this.findDefaultBlueprint(),
            this.planBlueprintService.query(this.initializeLookup()).pipe(
                takeUntil(this._destroyed)
			)
        ])
        .subscribe(([defaultBlueprint, blueprints]) => {
            this.defaultBlueprint = defaultBlueprint;
            this.planBlueprints = blueprints.items;
            this.planBlueprintCount = blueprints.count;
            if(this.selectedBlueprintId){ //pre-selected blueprint from input
                this.selectedBlueprint = this.selectedBlueprintId === this.defaultBlueprint?.groupId ? this.defaultBlueprint :
                    this.planBlueprints.find((x) => x.groupId === this.selectedBlueprintId)
            } else {
                this.selectedBlueprint = this.defaultBlueprint ?? this.planBlueprints?.[0];
            }
			this.getDescriptionTemplates(this.planBlueprints);
        })
    }
	getDescriptionTemplates(planBlueprints){
		let ids = [];
		for (let blueprint of planBlueprints) {
			if (blueprint && blueprint.definition?.sections) {
				const sectionWithDescriptionTemplates = blueprint.definition.sections.filter(x => x.hasTemplates == true && x.descriptionTemplates != null && x.descriptionTemplates.length > 0) || [];
				if (sectionWithDescriptionTemplates.length > 0) {
					for (let section of sectionWithDescriptionTemplates) {
						for (let descriptionTemplate of section.descriptionTemplates) {
							if(descriptionTemplate.descriptionTemplate?.groupId && !this.descriptionTemplates.has(descriptionTemplate.descriptionTemplate.groupId)) {
								this.descriptionTemplates.set(descriptionTemplate.descriptionTemplate.groupId, []);
								ids.push(descriptionTemplate.descriptionTemplate.groupId);
							}
						}
					}
				}
			}

		}
		if (ids.length > 0) {
			this.descriptionTemplateService.query(this.initializeDescriptionTemplateLookup(ids)).pipe(
				takeUntil(this._destroyed),
				tap((descriptionTemplates: QueryResult<DescriptionTemplate>) => {
				})).subscribe(queryResults => {
				for (let descriptionTemplate of queryResults.items) {
					this.descriptionTemplates.get(descriptionTemplate.groupId.toString()).push(descriptionTemplate);
				}

			})
		}
	}
	protected initializeLookup(groupId?: Guid): PlanBlueprintLookup {
		const lookup = new PlanBlueprintLookup();
		lookup.metadata = {countAll: true};
		lookup.page = {offset: 0, size: this.page * this.pageSize};
		lookup.like=this.searchTerm;
		if (groupId) { lookup.groupIds = [groupId]; }
		lookup.isActive = [IsActive.Active];
		lookup.statuses = [PlanBlueprintStatus.Finalized]
		lookup.versionStatuses = [PlanBlueprintVersionStatus.Current]
		lookup.order = {items: ['+' + nameof<PlanBlueprint>(x => x.label)]};
		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	protected initializeDescriptionTemplateLookup(ids): DescriptionTemplateLookup {
		const lookup = new DescriptionTemplateLookup();
		lookup.metadata = {countAll: true};
		lookup.page = {offset: 0, size: ids.length};
		lookup.isActive = [IsActive.Active];
		lookup.groupIds = ids;
		lookup.statuses = [DescriptionTemplateStatus.Finalized]
		lookup.versionStatuses = [DescriptionTemplateVersionStatus.Current]
		lookup.order = {items: ['-' + nameof<DescriptionTemplate>(x => x.createdAt)]};
		lookup.project = {
			fields: this.descriptionTemplateLookupFields
		};

		return lookup;
	}

	exportToPDF(name) {
		this.exportingPDF = true;
		this.expandAll();
		let divs = [];
		if (document.getElementById('blueprint-title')) {
			divs.push(document.getElementById('blueprint-title'));
		}
		if (document.getElementById('blueprint-description')) {
			divs.push(document.getElementById('blueprint-description'));
		}
		for (let i = 0; i < this.selectedBlueprint.definition.sections.length; i++) {
			// if section has templates, add divs one by one to fit in PDF pages
			if (this.selectedBlueprint.definition.sections[i].hasTemplates && this.selectedBlueprint.definition.sections[i].descriptionTemplates && this.selectedBlueprint.definition.sections[i].descriptionTemplates.length > 0) {

				if (document.getElementById('blueprint-section-' + i + '-title')) {
					divs.push(document.getElementById('blueprint-section-' + i + '-title'));
				}
				if (document.getElementById('blueprint-section-' + i + '-subtitle')) {
					divs.push(document.getElementById('blueprint-section-' + i + '-subtitle'));
				}
				if (document.getElementById('blueprint-section-' + i + '-fields')) {
					divs.push(document.getElementById('blueprint-section-' + i + '-fields'));
				}
				if (document.getElementById('blueprint-section-' + i + '-descriptionsMessage')) {
					divs.push(document.getElementById('blueprint-section-' + i + '-descriptionsMessage'));
				}
				for (let x = 0; x < this.selectedBlueprint.definition.sections[i].descriptionTemplates.length; x++) {

					let template: DescriptionTemplate =
						this.descriptionTemplates.get(this.selectedBlueprint.definition.sections[i].descriptionTemplates[x].descriptionTemplate?.groupId)[0];
					if (document.getElementById('blueprint-section-' + i + '-template-' + x + '-title')) {
						divs.push(document.getElementById('blueprint-section-' + i + '-template-' + x + '-title'));
					}
					if (template.definition && template.definition.pages) {
						for (let y = 0; y < template.definition.pages.length; y++) {
							if (document.getElementById('blueprint-section-' + i + '-template-' + x + '-page-' + y)) {
								divs.push(document.getElementById('blueprint-section-' + i + '-template-' + x + '-page-' + y));
							}

						}
					}
				}

			} else { // no templates add section altogether
				if (document.getElementById('blueprint-section-' + i )) {
					divs.push(document.getElementById('blueprint-section-' + i));
				}
			}
		}

		setTimeout(() => {
			this.export(divs, "Preview of blueprint_" + name).then(() => {
				this.exportingPDF = false;
			}).catch((error) => {
				this.exportingPDF = false;
				console.error('Error during PDF generation:', error);
			});

		}, 500);
	}
	export(contents: HTMLElement[], filename: string = 'exported-page.pdf'): Promise<void> {
		return new Promise((resolve, reject) => {
			const pdf = new JsPDF('p', 'mm', 'a4');
			const pageHeight = 297; // A4 page height in mm
			const pageWidth = 210; // A4 page width in mm
			const margin = 3; // Add a margin to leave space around content

			// Define the watermark image and dimensions
			const watermarkImg = document.getElementById('watermark') as HTMLImageElement;
			const watermarkData = watermarkImg?.src || '';
			const watermarkWidth = 200; // Adjust watermark width
			const watermarkHeight = 287; // Adjust watermark height

			// Helper function to add a watermark to each page
			const addWatermark = () => {
				let offset = 15
				if (watermarkData) {
					const watermarkX = (pageWidth - watermarkWidth) / 2; // Center horizontally
					const watermarkY = (pageHeight - watermarkHeight) / 2; // Center vertically
					pdf.addImage(watermarkData, 'PNG', watermarkX, watermarkY, watermarkWidth, watermarkHeight);
				} else {
					// Fallback to text watermark if no image is available
					pdf.setFontSize(50);
					pdf.setTextColor(150, 150, 150); // Light gray with transparency
					pdf.text('VIEW ONLY', pageWidth / 2 + offset, pageHeight / 2, {
						align: 'center',
						angle: 45,
					});
				}
			};
			// Create a temporary container to hold grouped divs
			const tempContainer = document.createElement('div');
			tempContainer.style.position = 'absolute';
			tempContainer.style.top = '-9999px'; // Place off-screen
			tempContainer.style.left = '-9999px'; // Place off-screen
			document.body.appendChild(tempContainer);

			// Function to group smaller divs into a single container
			const groupDivs = (): HTMLElement[] => {
				const groupedDivs: HTMLElement[] = [];
				let tempDiv = document.createElement('div'); // Temporary container
				tempDiv.style.width = `${pageWidth}mm`; // Match PDF width
				let currentHeight = 0;

				contents.forEach((element, index) => {
					const elementClone = element.cloneNode(true) as HTMLElement; // Clone the original div
					tempContainer.appendChild(elementClone); // Attach to temp container for accurate height calculation

					const elementHeight = elementClone.getBoundingClientRect().height * 0.264583; // Convert px to mm
					tempContainer.removeChild(elementClone); // Remove after calculation

					if (currentHeight + elementHeight <= pageHeight - 2 * margin) {
						// Add the current element to the tempDiv
						tempDiv.appendChild(elementClone);
						currentHeight += elementHeight;
					} else {
						// Push the current container to the list and create a new one
						groupedDivs.push(tempDiv);
						tempDiv = document.createElement('div'); // Create a new container
						tempDiv.style.width = `${pageWidth}mm`; // Match PDF width
						tempDiv.appendChild(elementClone); // Add the current element
						currentHeight = elementHeight; // Reset height tracker
					}

					// If it's the last element, add the tempDiv to the list
					if (index === contents.length - 1) {
						groupedDivs.push(tempDiv);
					}
				});

				return groupedDivs;
			};

			const groupedContents = groupDivs();

			const processElement = (index: number) => {
				if (index >= groupedContents.length) {

					addWatermark(); // in last page
					// All elements processed, save the PDF
					document.body.removeChild(tempContainer); // Clean up temporary container
					pdf.save( filename.endsWith(".pdf") ? filename : filename.replace(".xml", "") + ".pdf");
					resolve();
					return;
				}

				const element = groupedContents[index];
				tempContainer.appendChild(element); // Append to temp container for rendering

				html2canvas(element, { scale: 1.5 }).then(canvas => {
					const imgData = canvas.toDataURL('image/png');
					tempContainer.removeChild(element); // Remove after rendering

					// Scale the image width to fit within the page width
					let imgWidth = pageWidth - 2 * margin;
					let imgHeight = (canvas.height * imgWidth) / canvas.width;

					// Check if the image height exceeds the page height
					if (imgHeight > pageHeight - 2 * margin) {
						const scale = (pageHeight - 2 * margin) / imgHeight;
						imgHeight *= scale;
						imgWidth *= scale;
					}

					// Add the image to the PDF
					pdf.addImage(imgData, 'PNG', margin, margin, imgWidth, imgHeight);

					// Add a new page if not the last element
					if (index < groupedContents.length - 1) {
						addWatermark(); // Add the watermark to the prev page
						pdf.addPage();
					}

					// Process the next grouped container
					processElement(index + 1);
				}).catch(error => {
					document.body.removeChild(tempContainer); // Clean up on error
					reject(error);
				});
			};

			// Start processing grouped divs
			processElement(0);
		});
	}

	findDefaultBlueprint(): Observable<PlanBlueprint> {
		return this.tenantHandlingService.loadAndApplyTenantDefaultPlanBlueprint$().pipe(
            takeUntil(this._destroyed),
            catchError((error) => {this.httpErrorHandlingService.handleBackedRequestError(error); return of(null); }),
            switchMap((data: TenantConfiguration) => {
                if (data?.defaultPlanBlueprint?.groupId) {
                    return this.planBlueprintService.query(this.initializeLookup(data.defaultPlanBlueprint.groupId))
                    .pipe(
                        takeUntil(this._destroyed),
                        catchError((error) => {
                            this.httpErrorHandlingService.handleBackedRequestError(error);
                            return of(null);
                        }),
                        map((data: QueryResult<PlanBlueprint>) => {
                            return data?.items?.[0]
                        })
                    )
                }
                return of(null);
            })
        )
	}

	loadMore(){
		this.page++;
		this.planBlueprintService.query(this.initializeLookup()).pipe(
			takeUntil(this._destroyed)
		).subscribe(blueprints => {
			this.planBlueprints = blueprints.items
			this.getDescriptionTemplates(this.planBlueprints);
		});
	}

	search(){
		this.page = 1;
		this.planBlueprintService.query(this.initializeLookup()).pipe(
			takeUntil(this._destroyed)
		).subscribe(blueprints => {
			this.planBlueprints = blueprints.items
			this.planBlueprintCount = blueprints.count;
			this.getDescriptionTemplates(this.planBlueprints);
		});
	}

    expand(){
        this.baseExpansionPanels?.forEach((panel) => panel?.open());
    }

    expandAll(){
        this.baseExpansionPanels?.forEach((panel) => panel?.open());
        this.nestedExpansionPanels?.forEach((panel) => panel?.open());
    }

    collapseAll(){
        this.baseExpansionPanels?.forEach((panel) => panel?.close());
        this.nestedExpansionPanels?.forEach((panel) => panel?.close());
    }

}
