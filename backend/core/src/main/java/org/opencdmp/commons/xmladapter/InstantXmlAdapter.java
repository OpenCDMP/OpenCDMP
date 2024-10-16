package org.opencdmp.commons.xmladapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class InstantXmlAdapter extends XmlAdapter<String, Instant> {
	public InstantXmlAdapter() {
	}

	@Override
	public Instant unmarshal(String stringValue) {
		return stringValue != null ? DateTimeFormatter.ISO_INSTANT.parse(stringValue, Instant::from) : null;
	}

	@Override
	public String marshal(Instant value) {
		return value != null ? DateTimeFormatter.ISO_INSTANT.format(value) : null;
	}
}
