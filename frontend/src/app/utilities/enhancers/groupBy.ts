interface Array<T> {
	groupBy<E, J>(by: (item: T) => J): Array<Array<E>>;
}

Array.prototype.groupBy = function (f: Function) {
	return this.reduce((ys: any, x: any) => {
		ys[f.call(this, x)] = ys[f.call(this, x)] || [];
		ys[f.call(this, x)].push(x);
		return ys;
	}, []);
};
