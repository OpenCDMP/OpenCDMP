package org.opencdmp.model.descriptiontemplate;

public class Multiplicity {

	public final static String _min = "min";
	private Integer min;

	public final static String _max = "max";
	private Integer max;

	public final static String _placeholder = "placeholder";
	private String placeholder;

	public final static String _tableView = "tableView";
	private Boolean tableView;

	public Integer getMin() {
		return min;
	}

	public void setMin(Integer min) {
		this.min = min;
	}

	public Integer getMax() {
		return max;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public Boolean getTableView() {
		return tableView;
	}

	public void setTableView(Boolean tableView) {
		this.tableView = tableView;
	}
}
