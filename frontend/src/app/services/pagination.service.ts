export class PaginationService {
	getPagination(groups, totalGroups: number, currentPage: number = 1, pageSize: number = 3) {
		// calculate total pages

		let totalPages = 0 ; //totalpages based on pages from xml, each group and section has each one page
		groups.forEach(group => {
			if (group.page > totalPages) {
				totalPages = parseInt(group.page);
			}
		});

		let startPage: number, endPage: number;
		if (totalPages <= 10) {
			// less than 10 total pages so show all
			startPage = 1;
			endPage = totalPages;
		} else {
			// more than 10 total pages so calculate start and end pages
			if (currentPage <= 6) {
				startPage = 1;
				endPage = 10;
			} else if (currentPage + 4 >= totalPages) {
				startPage = totalPages - 9;
				endPage = totalPages;
			} else {
				startPage = currentPage - 5;
				endPage = currentPage + 4;
			}
		}

		// calculate start and end item indexes
		const startIndex = (currentPage - 1) * pageSize;
		const endIndex = Math.min(startIndex + pageSize - 1, totalGroups - 1);

		// create an array of pages to ng-repeat in the pagination control
		const pages = [];
		for (let i = 1; i < endPage + 1; i++) {
			pages.push(i);
		}

		// return object with all pagination properties required by the view
		return {
			totalGroups: totalGroups,
			currentPage: currentPage,
			pageSize: pageSize,
			totalPages: totalPages,
			startPage: startPage,
			endPage: endPage,
			startIndex: startIndex,
			endIndex: endIndex,
			pages: pages
		};
	}
}
