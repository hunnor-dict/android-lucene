package net.hunnor.dict.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.hunnor.dict.LuceneConstants;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

@SuppressWarnings("deprecation")
public class Dictionary implements LuceneConstants {

	private File indexDirectory = null;
	private IndexReader indexReader = null;
	private Analyzer analyzer = null;

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
		try {
			indexSearcher = new IndexSearcher(indexReader);
			QueryParser queryParser =
					new QueryParser(LUCENE_VERSION, LUCENE_FIELD_ID, analyzer);
			StringBuilder sb = new StringBuilder();
			sb.append(LUCENE_FIELD_HU_ROOTS)
					.append(":").append(queryString).append("*");
			sb.append(" ");
			sb.append(LUCENE_FIELD_NO_ROOTS)
					.append(":").append(queryString).append("*");
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

	private boolean constructAnalyzer() {
		try {
			KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
			StandardAnalyzer standardAnalyzer = new StandardAnalyzer(LUCENE_VERSION);
			SnowballAnalyzer norwegianSnowballAnalyzer = new SnowballAnalyzer(LUCENE_VERSION, "Norwegian", CharArraySet.EMPTY_SET);
			SnowballAnalyzer hungarianSnowballAnalyzer = new SnowballAnalyzer(LUCENE_VERSION, "Hungarian", CharArraySet.EMPTY_SET);

			Map<String, Analyzer> mapping = new HashMap<String, Analyzer>();
			mapping.put(LUCENE_FIELD_HU_ROOTS, standardAnalyzer);
			mapping.put(LUCENE_FIELD_NO_ROOTS, standardAnalyzer);
			mapping.put(LUCENE_FIELD_HU_FORMS, standardAnalyzer);
			mapping.put(LUCENE_FIELD_NO_FORMS, standardAnalyzer);
			mapping.put(LUCENE_FIELD_HU_TRANS, norwegianSnowballAnalyzer);
			mapping.put(LUCENE_FIELD_NO_TRANS, hungarianSnowballAnalyzer);
			mapping.put(LUCENE_FIELD_HU_QUOTE, hungarianSnowballAnalyzer );
			mapping.put(LUCENE_FIELD_NO_QUOTE, norwegianSnowballAnalyzer );
			mapping.put(LUCENE_FIELD_HU_QUOTETRANS, norwegianSnowballAnalyzer );
			mapping.put(LUCENE_FIELD_NO_QUOTETRANS, hungarianSnowballAnalyzer );

			analyzer = new PerFieldAnalyzerWrapper(keywordAnalyzer, mapping);
		} catch (Exception exception) {
			return false;
		}
		return true;
	}

}
