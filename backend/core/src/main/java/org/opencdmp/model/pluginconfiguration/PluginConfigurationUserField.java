package org.opencdmp.model.pluginconfiguration;

import org.opencdmp.model.StorageFile;

public class PluginConfigurationUserField {

    private String code;
    public final static String _code = "code";

    private StorageFile fileValue;
    public final static String _fileValue = "fileValue";

    private String textValue;
    public final static String _textValue = "textValue";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public StorageFile getFileValue() {
        return fileValue;
    }

    public void setFileValue(StorageFile fileValue) {
        this.fileValue = fileValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }
}
