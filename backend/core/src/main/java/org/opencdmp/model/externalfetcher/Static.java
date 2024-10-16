package org.opencdmp.model.externalfetcher;


import java.util.List;

public class Static {

    private List<StaticOption> options;
    public final static String _options = "options";

    public List<StaticOption> getOptions() {
        return options;
    }

    public void setOptions(List<StaticOption> options) {
        this.options = options;
    }
}
