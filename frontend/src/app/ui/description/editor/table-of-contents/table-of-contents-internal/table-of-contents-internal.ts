import {
  booleanAttribute,
  Component,
  EventEmitter,
  input,
  Input,
  OnDestroy,
  OnInit,
  Optional,
  Output,
  QueryList,
  ViewChildren,
  ChangeDetectionStrategy
} from '@angular/core';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { BaseComponent } from '@common/base/base.component';
import { ToCEntry } from '../models/toc-entry';
import { MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelContent } from '@angular/material/expansion';
import { PlanAnnotationService } from '@app/ui/plan/plan-editor-blueprint/plan-annotation.service';
import { AnnotationEntityType } from '@app/core/common/enum/annotation-entity-type';
import { TableOfContentsHelper } from '../helpers/table-of-contents.helper';
import { Guid } from '@common/types/guid';
import { NgClass, NgTemplateOutlet } from '@angular/common';
import { MatIcon } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'table-of-contents-internal',
    styleUrls: ['./table-of-contents-internal.scss'],
    templateUrl: './table-of-contents-internal.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatExpansionPanel, MatExpansionPanelHeader, NgClass, NgTemplateOutlet, MatExpansionPanelContent, MatIcon, TranslatePipe]
})
export class TableOfContentsInternal extends BaseComponent implements OnInit, OnDestroy {
    @ViewChildren('expansionPanel') expansionPanels: QueryList<MatExpansionPanel>
    @ViewChildren('internalTable') internalTables: QueryList<TableOfContentsInternal>
	@Input() tocentries: ToCEntry[] = null;
	@Input() selected: ToCEntry = null;
	@Output() entrySelected = new EventEmitter<ToCEntry>();
    @Input() level: number = 1;

	// expandChildren: boolean[];
	@Input() showErrors: boolean = false;
	@Input() visibilityRulesService: VisibilityRulesService;
    @Input({transform: booleanAttribute}) isTopLevel: boolean = false;

    numbering = input<string>();
	descriptionId = input<Guid>();
	planId = input<Guid>();

	annotationsPerEntry = input<Map<string, number>>(new Map<string, number>([]));
	constructor(@Optional() private planAnnotationService: PlanAnnotationService) { super(); }

    isOrContainsSelected(entry: ToCEntry): boolean {
        if (!this.selected) { return false; }
        // Descriptions built from the same template share entry ids, so an id-only match would
        // highlight (and expand) the same entry in every description. Scope to THIS description: the
        // selected entry must belong to it — its pathToEntry includes our descriptionId.
        const descId = this.descriptionId()?.toString();
        if (descId && !this.selected.pathToEntry?.some(p => p?.toString() === descId)) {
            return false;
        }
        return entry.id === this.selected.id || (this.selected.pathToEntry?.includes(entry.id) ?? false);
    }

	ngOnInit(): void {
		// if (this.tocentries) {
		// 	this.expandChildren = this.tocentries.map(() => false);
		// 	if (this.selected) {
		// 		for (let i = 0; i < this.tocentries.length; i++) {
		// 			if (TableOfContentsComponent._findTocEntryById(this.selected.id, this.tocentries[i].subEntries)) {
		// 				if (this.expandChildren) {
		// 					this.expandChildren[i] = true;
		// 				}
		// 				break;
		// 			}
		// 		}
		// 	}
		// }
	}

	// ngOnChanges(changes: SimpleChanges) {
	// 	if (changes.selected && this.selected) {
	// 		for (let i = 0; i < this.tocentries.length; i++) {
	// 			if (TableOfContentsComponent._findTocEntryById(this.selected.id, this.tocentries[i].subEntries)) {
	// 				if (this.expandChildren) {
	// 					this.expandChildren[i] = true;
	// 				}
	// 				break;
	// 			}
	// 		}
	// 	}
	// }

	ngOnDestroy(): void {
		super.ngOnDestroy();
	}

	// toggleExpand(index, entry: ToCEntry) {
	// 	this.expandChildren[index] = !this.expandChildren[index];
    //     if(this.expandChildren[index]) {
    //         this.onEntrySelected(entry);
    //     }
	// }

	onEntrySelected(entry: ToCEntry) {
		this.entrySelected.emit(entry);
	}



	// calculateClass(entry: ToCEntry) {
	// 	return {
    //         'selected': this.selected && (entry.id === this.selected.id || this.selected.pathToEntry?.[0] === entry.id),
    //         'section': entry.type != ToCEntryType.FieldSet,
    //         'field-set': entry.type === ToCEntryType.FieldSet
    //     };
	// }

	isTocEntryValid(entry: ToCEntry): boolean {
        return TableOfContentsHelper.isTocEntryValid(entry);
		// if (entry == null) return true;

		// if (entry.validityAbstractControl && (!entry.validityAbstractControl.touched || entry.validityAbstractControl.disabled)){
        //     return true;
        // }
		// let currentValidity = entry.validityAbstractControl?.valid ?? true;
		// if (!currentValidity) return currentValidity;
		// entry.subEntries?.forEach(subEntry => {
		// 	currentValidity = currentValidity && this.isTocEntryValid(subEntry);
		// 	if (!currentValidity){
        //         return currentValidity;
        //     }
		// });

		// return currentValidity;
	}
	showAnnotations(fieldSetId: string){
        this.planAnnotationService?.setContext({
            anchor: fieldSetId,
            entityId: this.descriptionId() ? this.descriptionId() : this.planId(),
            entityType: this.descriptionId() ? AnnotationEntityType.Description :  AnnotationEntityType.Plan
        })
		// this.formAnnotationService = this.formAnnotationServices?.find(service => service.getEntityId() === this.descriptionId());
		//  if(this.formAnnotationService) {
		// 	 this.formAnnotationService.openAnnotationDialog(fieldSetId);
		//  }
	}

    public expandL1(){
        this.expansionPanels?.forEach((panel) => {
            panel.open();
        })
    }

    public expandAll(){
        this.expandL1();
        this.internalTables?.forEach((tableComponent) => {
            tableComponent.expandAll();
        })
    }

    public collapseAll(){
        this.expansionPanels?.forEach((panel) => {
            panel.close();
        })
        this.internalTables?.forEach((tableComponent) => {
            tableComponent.collapseAll();
        })
    }
}
