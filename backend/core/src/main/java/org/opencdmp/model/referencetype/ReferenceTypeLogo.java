package org.opencdmp.model.referencetype;

/** The logo of a reference type: the image of the first of its sources that has one. */
public class ReferenceTypeLogo {

    private final byte[] content;

    private final String mimeType;

    public ReferenceTypeLogo(byte[] content, String mimeType) {
        this.content = content;
        this.mimeType = mimeType;
    }

    public byte[] getContent() {
        return content;
    }

    public String getMimeType() {
        return mimeType;
    }
}
