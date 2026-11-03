package org.opencdmp.model.tenantconfiguration;

public class DashboardSlideTranslation {

    private String languageCode;
    public static final String _languageCode = "languageCode";

    private String title;
    public static final String _title = "title";

    private String text;
    public static final String _text = "text";

    private String buttonText;
    public static final String _buttonText = "buttonText";

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }
}
