package org.opencdmp.model.externalfetcher;

public class XmlNamespaceConfiguration {
    public final static String _prefix = "prefix";
    private String prefix;

    public final static String _uri = "uri";
    private String uri;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
