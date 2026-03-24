package org.opencdmp.commons.enums.rdamadmp;

import com.fasterxml.jackson.annotation.JsonValue;
import gr.cite.tools.exception.MyApplicationException;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.HashMap;
import java.util.Map;

public enum CertifiedWithType implements DatabaseEnum<String> {

    DIN_31644("din31644"),
    DINI_ZERTIFIKAT("dini-zertifikat"),
    DSA("dsa"),
    ISO_16363("iso16363"),
    ISO_16919("iso16919"),
    TRAC("trac"),
    WDS("wds"),
    CORETRUSTSEAL("coretrustseal");
    private final String value;
    private final static Map<String, CertifiedWithType> CONSTANTS = new HashMap<String, CertifiedWithType>();

    CertifiedWithType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return this.value;
    }

    private static final Map<String, CertifiedWithType> map = EnumUtils.getEnumValueMap(CertifiedWithType.class);

    public static CertifiedWithType of(String i) {
        return map.get(i);
    }

    public static CertifiedWithType fromValue(String value) {
        CertifiedWithType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new MyApplicationException(value);
        } else {
            return constant;
        }
    }

}
