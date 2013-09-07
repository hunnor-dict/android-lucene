package net.hunnor.dict;

public class IndexObject {

	private String id;

	private String lang;

	private String text;

	public IndexObject () {
		super();
	}

	public IndexObject(String id, String lang, String text) {
		this.id = id;
		this.lang = lang;
		this.text = text;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
