package org.opencdmp.commons.types.notification;

import java.util.List;

public class NotificationFieldData {
	private List<FieldInfo> fields;
	private List<Attachment> attachments;

	public NotificationFieldData(List<FieldInfo> fields, List<Attachment> attachments) {
		this.fields = fields;
		this.attachments = attachments;
	}

	public NotificationFieldData() {
	}

	public List<FieldInfo> getFields() {
		return fields;
	}

	public void setFields(List<FieldInfo> fields) {
		this.fields = fields;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}
}
