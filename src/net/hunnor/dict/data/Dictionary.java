package net.hunnor.dict.data;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.hunnor.dict.LuceneConstants;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.hu.HungarianAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

public class Dictionary implements LuceneConstants {

	private Analyzer analyzer = null;
	private File indexDirectory = null;
	private IndexReader indexReader = null;
	private SpellChecker spellChecker = null;
	private File spellingDirectory = null;

	public boolean open() {
		return
				indexDirectory != null &&
				indexReader != null &&
				analyzer != null;
	}

	public boolean open(File indexDirectory) {
		this.indexDirectory = indexDirectory;
		return constructIndexReader() && constructAnalyzer();
	}

	public boolean openSpellChecker(File spellingDirectory) {
		this.spellingDirectory = spellingDirectory;
		return constructSpellChecker();
	}

	public List<Entry> suggest(String queryString) {
		if (indexReader == null) {
			if (!constructIndexReader()) {
				return null;
			}
		}
		if (analyzer == null) {
			if (!constructAnalyzer()) {
				return null;
			}
		}

		List<Entry> results = new ArrayList<Entry>();
		IndexSearcher indexSearcher = null;
		String analyzedQuery = null;
		try {
			indexSearcher = new IndexSearcher(indexReader);
			QueryParser queryParser =
					new QueryParser(LUCENE_VERSION, LUCENE_FIELD_ID, analyzer);
			analyzedQuery = analyzeQuery(queryString);
			Query query = queryParser.parse(analyzedQuery);
			TopDocs topDocs = indexSearcher.search(query, indexReader.maxDoc());
			ScoreDoc[] scoreDoc = topDocs.scoreDocs;
			int totalHits = topDocs.totalHits;
			for (int i = 0; i < totalHits; i++) {
				Document document = indexSearcher.doc(scoreDoc[i].doc);
				String root = getRoots(document);
				Entry entry = new Entry(
						document.get(LUCENE_FIELD_ID),
						document.get(LUCENE_FIELD_LANG),
						root,
						document.get(LUCENE_FIELD_TEXT));
				results.add(entry);
			}
		} catch (ParseException e) {
			return null;
		} catch (IOException e) {
			return null;
		} finally {
			if (indexSearcher != null) {
				try {
					indexSearcher.close();
				} catch (IOException e) {
					return null;
				}
			}
		}
		return results;
	}

	public String[] spellCheck(String queryString) {
		String[] suggestions = null;
		try {
			suggestions = spellChecker.suggestSimilar(queryString, 5);
		} catch (IOException exception) {
			return null;
		}
		return suggestions;
	}

	public List<Entry> lookup(String queryString, String lang) {
		if (indexReader == null) {
			if (!constructIndexReader()) {
				return null;
			}
		}
		if (analyzer == null) {
			if (!constructAnalyzer()) {
				return null;
			}
		}

		List<List<String>> runs = getRuns(lang);

		List<Entry> results = new ArrayList<Entry>();
		IndexSearcher indexSearcher = null;
		try {
			indexSearcher = new IndexSearcher(indexReader);
			QueryParser queryParser =
					new QueryParser(LUCENE_VERSION, LUCENE_FIELD_ID, analyzer);
			StringBuilder sb = null;
			for (List<String> run: runs) {
				sb = new StringBuilder();
				for (String field: run) {
					sb.append(field).append(":").append(queryString).append(" ");					
				}
				Query query = queryParser.parse(sb.toString());
				TopDocs topDocs = indexSearcher.search(query, indexReader.maxDoc());
				ScoreDoc[] scoreDoc = topDocs.scoreDocs;
				int totalHits = topDocs.totalHits;
				for (int i = 0; i < totalHits; i++) {
					Document document = indexSearcher.doc(scoreDoc[i].doc);
					String root = getRoots(document);
					Entry entry = new Entry(
							document.get(LUCENE_FIELD_ID),
							document.get(LUCENE_FIELD_LANG),
							root,
							document.get(LUCENE_FIELD_TEXT));
					results.add(entry);
				}
				if (totalHits > 0) {
					break;
				}
			}
		} catch (ParseException e) {
			return null;
		} catch (IOException e) {
			return null;
		} finally {
			if (indexSearcher != null) {
				try {
					indexSearcher.close();
				} catch (IOException e) {
					return null;
				}
			}
		}
		return results;
	}

	@SuppressWarnings("unused")
	private String analyzeQuery(String query) throws IOException {
		StringBuilder sb = new StringBuilder();
		Reader reader = new StringReader(query);
		TokenStream tokenStream =
				analyzer.tokenStream(LUCENE_FIELD_HU_FORMS, reader);
		OffsetAttribute offsetAttribute =
				tokenStream.getAttribute(OffsetAttribute.class);
		CharTermAttribute charTermAttribute =
				tokenStream.addAttribute(CharTermAttribute.class);
		tokenStream.reset();
		while (tokenStream.incrementToken()) {
			int startOffset = offsetAttribute.startOffset();
			int endOffset = offsetAttribute.endOffset();
			sb.append(LUCENE_FIELD_HU_ROOTS).append(":");
			sb.append(charTermAttribute.toString()).append("*");
			sb.append(" ");
			sb.append(LUCENE_FIELD_NO_ROOTS).append(":");
			sb.append(charTermAttribute.toString()).append("*");
			sb.append(" ");
		}
		return sb.toString();
	}

	private String getRoots(Document document) {
		StringBuilder sb = new StringBuilder();
		String field = null;
		if (LANG_HU.equals(document.get(LUCENE_FIELD_LANG))) {
			field = LUCENE_FIELD_HU_ROOTS;
		} else if (LANG_NO.equals(document.get(LUCENE_FIELD_LANG))) {
			field = LUCENE_FIELD_NO_ROOTS;
		}
		String[] roots = document.getValues(field);
		for (String root: roots) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(root);
		}
		return sb.toString();
	}

	private List<List<String>> getRuns(String lang) {
		List<List<String>> runs = new ArrayList<List<String>>();
		List<String> run1 = new ArrayList<String>();
		List<String> run2 = new ArrayList<String>();
		List<String> run3 = new ArrayList<String>();
		if (LANG_HU.equals(lang)) {
			run1.add(LUCENE_FIELD_HU_ROOTS);
			run1.add(LUCENE_FIELD_HU_FORMS);
			run2.add(LUCENE_FIELD_NO_TRANS);
			run3.add(LUCENE_FIELD_HU_QUOTE);
			run3.add(LUCENE_FIELD_NO_QUOTETRANS);
		} else if (LANG_NO.equals(lang)) {
			run1.add(LUCENE_FIELD_NO_ROOTS);
			run1.add(LUCENE_FIELD_NO_FORMS);
			run2.add(LUCENE_FIELD_HU_TRANS);
			run3.add(LUCENE_FIELD_NO_QUOTE);
			run3.add(LUCENE_FIELD_HU_QUOTETRANS);
		} else {
			run1.add(LUCENE_FIELD_HU_ROOTS);
			run1.add(LUCENE_FIELD_HU_FORMS);
			run1.add(LUCENE_FIELD_NO_ROOTS);
			run1.add(LUCENE_FIELD_NO_FORMS);
			run2.add(LUCENE_FIELD_HU_TRANS);
			run2.add(LUCENE_FIELD_NO_TRANS);
			run3.add(LUCENE_FIELD_HU_QUOTE);
			run3.add(LUCENE_FIELD_NO_QUOTE);
			run3.add(LUCENE_FIELD_HU_QUOTETRANS);
			run3.add(LUCENE_FIELD_NO_QUOTETRANS);
		}
		runs.add(run1);
		runs.add(run2);
		runs.add(run3);
		return runs;
	}

	private boolean constructIndexReader() {
		try {
			Directory directory = new NIOFSDirectory(indexDirectory);
			indexReader = IndexReader.open(directory);
		} catch (CorruptIndexException exception) {
			return false;
		} catch (IOException exception) {
			return false;
		}
		return true;
	}

	private boolean constructSpellChecker() {
		try {
			Directory directory = new NIOFSDirectory(spellingDirectory);
			spellChecker = new SpellChecker(directory);
		} catch (IOException exception) {
			return false;
		}
		return true;
	}

	private boolean constructAnalyzer() {
		try {
			KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
			CustomAnalyzer customAnalyzer = new CustomAnalyzer(
					LUCENE_VERSION);
			HungarianAnalyzer hungarianAnalyzer = new HungarianAnalyzer(
					LUCENE_VERSION, CharArraySet.EMPTY_SET);
			NorwegianAnalyzer norwegianAnalyzer = new NorwegianAnalyzer(
					LUCENE_VERSION, CharArraySet.EMPTY_SET);

			Map<String, Analyzer> mapping = new HashMap<String, Analyzer>();
			mapping.put(LUCENE_FIELD_HU_ROOTS, customAnalyzer);
			mapping.put(LUCENE_FIELD_NO_ROOTS, customAnalyzer);
			mapping.put(LUCENE_FIELD_HU_FORMS, customAnalyzer);
			mapping.put(LUCENE_FIELD_NO_FORMS, customAnalyzer);
			mapping.put(LUCENE_FIELD_HU_TRANS, norwegianAnalyzer);
			mapping.put(LUCENE_FIELD_NO_TRANS, hungarianAnalyzer);
			mapping.put(LUCENE_FIELD_HU_QUOTE, hungarianAnalyzer );
			mapping.put(LUCENE_FIELD_NO_QUOTE, norwegianAnalyzer );
			mapping.put(LUCENE_FIELD_HU_QUOTETRANS, norwegianAnalyzer );
			mapping.put(LUCENE_FIELD_NO_QUOTETRANS, hungarianAnalyzer );

			analyzer = new PerFieldAnalyzerWrapper(keywordAnalyzer, mapping);
		} catch (Exception exception) {
			return false;
		}
		return true;
	}

}
