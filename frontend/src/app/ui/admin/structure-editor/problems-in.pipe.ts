import { Pipe, PipeTransform } from '@angular/core';
import { EditorTab, StructureProblem } from './structure-problems';

/** Problems that live on a given tab, for the counts in the tab strip. */
@Pipe({
    name: 'bp2ProblemsIn', pure: true
})
export class Bp2ProblemsInPipe implements PipeTransform {
	transform(problems: StructureProblem[], tab: EditorTab): StructureProblem[] {
		return (problems ?? []).filter(p => p.tab === tab);
	}
}
