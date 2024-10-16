interface Array<T> {
	flatMap<E>(callback: (t: T) => Array<E>): Array<E>;
}

Array.prototype.flatMap = function (f: Function) {
	return this.reduce((ys: any, x: any) => {
		return ys.concat(f.call(this, x));
	}, []);
};
