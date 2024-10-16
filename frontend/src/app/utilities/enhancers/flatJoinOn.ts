interface Array<T> {
	flatJoinOn<E, J>(by: (item: T) => J): Array<E>;
}

Array.prototype.flatJoinOn = function (f: Function) {
	return this.groupBy(f).reduce((ys: any, x: any) => {
		return ys.concat(f.call(this, x));
	}, []);
};
