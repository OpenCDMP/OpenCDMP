import { Pipe, PipeTransform } from '@angular/core';

/**
 * Splits a semantic key after each dot, so a template can put a line-break opportunity there and a long key in a
 * narrow chip wraps at `rda.dataset.` rather than mid-word. Usage: `@for (p of key | bp2KeyParts; track $index) {<span>{{p}}</span><wbr>}`.
 */
@Pipe({ name: 'bp2KeyParts' })
export class Bp2KeyPartsPipe implements PipeTransform {
	transform(key: string): string[] {
		return (key ?? '').split(/(?<=\.)/);
	}
}
