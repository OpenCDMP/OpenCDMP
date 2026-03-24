package org.opencdmp.commons.enums.rdamadmp;

import com.fasterxml.jackson.annotation.JsonValue;
import gr.cite.tools.exception.MyApplicationException;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.HashMap;
import java.util.Map;

public enum PidSystemType implements DatabaseEnum<String> {

    ARK("ark"),
    ARXIV("arxiv"),
    BIBCODE("bibcode"),
    DOI("doi"),
    EAN_13("ean13"),
    EISSN("eissn"),
    HANDLE("handle"),
    IGSN("igsn"),
    ISBN("isbn"),
    ISSN("issn"),
    ISTC("istc"),
    LISSN("lissn"),
    LSID("lsid"),
    PMID("pmid"),
    PURL("purl"),
    UPC("upc"),
    URL("url"),
    URN("urn"),
    OTHER("other");
    private final String value;
    private final static Map<String, PidSystemType> CONSTANTS = new HashMap<String, PidSystemType>();

    PidSystemType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return this.value;
    }

    private static final Map<String, PidSystemType> map = EnumUtils.getEnumValueMap(PidSystemType.class);

    public static PidSystemType of(String i) {
        return map.get(i);
    }

    public static PidSystemType fromValue(String value) {
        PidSystemType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new MyApplicationException(value);
        } else {
            return constant;
        }
    }

}
