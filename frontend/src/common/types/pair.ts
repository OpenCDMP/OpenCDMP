export class Pair<L, R> {
	public readonly left: L;
	public readonly right: R;

	constructor(left: L, right: R) {
		this.left = left;
		this.right = right;
	}
}