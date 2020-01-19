package net.hunnor.dict.android.service;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class StorageService {

    public static final String DICTIONARY_INDEX_DIRECTORY = "hunnor-lucene-index";

    public static final String DICTIONARY_SPELLING_DIRECTORY = "hunnor-lucene-spelling";

    public static final String DATE = "date";

    public static final String SIZE = "size";

    public Map<String, Long> fileStats(File... directories) {

        long timestamp = 0;
        long size = 0;

        for (File directory : directories) {
            if (directory.canRead() && directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.canRead() && file.isFile()) {
                            size = size + file.length();
                            long lastModified = file.lastModified();
                            if (lastModified > timestamp) {
                                timestamp = lastModified;
                            }
                        }
                    }
                }
            }
        }

        Map<String, Long> results = new HashMap<>();
        results.put(DATE, timestamp);
        results.put(SIZE, size);

        return results;

    }

    public void deleteRecursively(File file) throws IOException {
        FileUtils.deleteDirectory(file);
    }

    public void extract(InputStream inputStream, File baseDirectory, String suffix) throws IOException {

        byte[] buffer = new byte[256 * 1024];

        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        while (zipEntry != null) {
            String fileName = zipEntry.getName();
            fileName = fileName.replace(DICTIONARY_INDEX_DIRECTORY,
                    DICTIONARY_INDEX_DIRECTORY + suffix);
            fileName = fileName.replace(DICTIONARY_SPELLING_DIRECTORY,
                    DICTIONARY_SPELLING_DIRECTORY + suffix);
            File entryFile = new File(baseDirectory, fileName);
            String directory = entryFile.getParent();
            if (directory != null) {
                File entryDirectory = new File(directory);
                if (!entryDirectory.isDirectory()) {
                    if (!entryDirectory.mkdirs()) {
                        throw new IOException();
                    }
                }
            }
            FileOutputStream fileOutputStream = new FileOutputStream(entryFile);
            int length;
            while ((length = zipInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }
            fileOutputStream.close();
            long timestamp = zipEntry.getTime();
            entryFile.setLastModified(timestamp);
            zipEntry = zipInputStream.getNextEntry();
        }

        zipInputStream.closeEntry();
        zipInputStream.close();

    }

    public boolean rename(File from, File to) {
        return from.renameTo(to);
    }

}
