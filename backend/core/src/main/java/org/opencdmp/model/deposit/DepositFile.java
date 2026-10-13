package org.opencdmp.model.deposit;

import org.opencdmp.depositbase.enums.DepositFileKind;

import java.util.List;

public class DepositFile {

    private DepositFileKind kind;
    public static final String _kind = "kind";

    private String filename;
    public static final String _filename = "filename";

    private List<String> contents;
    public static final String _contents = "contents";

    public DepositFileKind getKind() {
        return kind;
    }

    public void setKind(DepositFileKind kind) {
        this.kind = kind;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public List<String> getContents() {
        return contents;
    }

    public void setContents(List<String> contents) {
        this.contents = contents;
    }
}
