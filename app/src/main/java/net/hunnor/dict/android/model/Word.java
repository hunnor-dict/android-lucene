package net.hunnor.dict.android.model;

public class Word {

    public enum Source {
        ROOTS,
        SPELLING,
        HISTORY
    }

    private String value;

    private Source source;

    public Word(String value, Source source) {
        this.value = value;
        this.source = source;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

}
