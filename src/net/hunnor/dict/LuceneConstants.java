package net.hunnor.dict;

import org.apache.lucene.util.Version;

public interface LuceneConstants {

	public static String LANG_HU = "hu";
	public static String LANG_NO = "no";

	public static String INDEX_DIR = "index";
	public static String INDEX_ZIP = "hunnor-lucene-3.6.zip";
	public static String INDEX_URL = "http://dict.hunnor.net/port/lucene/hunnor-lucene-3.6.zip";

	public static final Version LUCENE_VERSION = Version.LUCENE_36;

	public static final String LUCENE_FIELD_ID = "id";
	public static final String LUCENE_FIELD_LANG = "lang";
	public static final String LUCENE_FIELD_TEXT = "text";

	public static final String LUCENE_FIELD_HU_ROOTS = "hu_roots";
	public static final String LUCENE_FIELD_NO_ROOTS = "no_roots";
	public static final String LUCENE_FIELD_HU_FORMS = "hu_forms";
	public static final String LUCENE_FIELD_NO_FORMS = "no_forms";
	public static final String LUCENE_FIELD_HU_TRANS = "hu_trans";
	public static final String LUCENE_FIELD_NO_TRANS = "no_trans";
	public static final String LUCENE_FIELD_HU_QUOTE = "hu_quote";
	public static final String LUCENE_FIELD_NO_QUOTE = "no_quote";
	public static final String LUCENE_FIELD_HU_QUOTETRANS = "hu_quoteTrans";
	public static final String LUCENE_FIELD_NO_QUOTETRANS = "no_quoteTrans";

}
