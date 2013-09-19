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

		List<Entry> results = new ArrayList<Entry>();
		IndexSearcher indexSearcher = null;
		try {
			indexSearcher = new IndexSearcher(indexReader);
			StringBuilder sb = new StringBuilder();
			String[] fields = {LUCENE_FIELD_HU_ROOTS, LUCENE_FIELD_HU_FORMS, LUCENE_FIELD_HU_TRANS, LUCENE_FIELD_NO_ROOTS, LUCENE_FIELD_NO_FORMS, LUCENE_FIELD_NO_TRANS};
			for (String field: fields) {
				sb.append(field).append(":").append(queryString).append(" ");
			}
			QueryParser queryParser = new QueryParser(LUCENE_VERSION, LUCENE_FIELD_ID, analyzer);
			Query query = queryParser.parse(sb.toString());
			TopDocs topDocs = indexSearcher.search(query, indexReader.maxDoc());
			ScoreDoc[] scoreDoc = topDocs.scoreDocs;
			int totalHits = topDocs.totalHits;
			for (int i = 0; i < totalHits; i++) {
				Document document = indexSearcher.doc(scoreDoc[i].doc);
				Entry entry = new Entry(
						document.get(LUCENE_FIELD_ID),
						document.get(LUCENE_FIELD_LANG),
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
