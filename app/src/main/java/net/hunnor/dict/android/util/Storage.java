package net.hunnor.dict.android.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Storage {

    public enum Status {

        E_DEPLOY_DELETE_DEPLOY_INDEX_DIR,

        E_DEPLOY_DELETE_DEPLOY_SPELLING_DIR,

        E_DEPLOY_DELETE_INDEX_DIR,

        E_DEPLOY_DELETE_SPELLING_DIR,

        E_DEPLOY_RENAME_INDEX_DIR,

        E_DEPLOY_RENAME_SPELLING_DIR,

        E_DEPLOY_ZIP_ENTRY_DIR_CREATE,

        E_EXCEPTION_IO,

        OK

    }

    public static final String DICTIONARY_INDEX_DIRECTORY = "hunnor-lucene-index";

    public static final String DICTIONARY_SPELLING_DIRECTORY = "hunnor-lucene-spelling";

    public static Status deployDictionary(InputStream inputStream, File baseDirectory) {

        String suffix = "-deploy";

        // Delete files to make names available for extraction

        File indexDirectory = new File(baseDirectory,
                DICTIONARY_INDEX_DIRECTORY + suffix);
        if (recursiveDeleteFails(indexDirectory)) {
            return Status.E_DEPLOY_DELETE_DEPLOY_INDEX_DIR;
        }

        File spellingDirectory = new File(baseDirectory,
                DICTIONARY_SPELLING_DIRECTORY + suffix);
        if (recursiveDeleteFails(spellingDirectory)) {
            return Status.E_DEPLOY_DELETE_DEPLOY_SPELLING_DIR;
        }

        // Extract with temporary directory name

        try {

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
                File entryDirectory = new File(entryFile.getParent());
                if (!entryDirectory.isDirectory()) {
                    if (!entryDirectory.mkdirs()) {
                        return Status.E_DEPLOY_ZIP_ENTRY_DIR_CREATE;
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

        } catch (IOException e) {
            return Status.E_EXCEPTION_IO;
        }

        // Switch to new directories

        indexDirectory = new File(baseDirectory, DICTIONARY_INDEX_DIRECTORY);
        if (recursiveDeleteFails(indexDirectory)) {
            return Status.E_DEPLOY_DELETE_INDEX_DIR;
        }
        spellingDirectory = new File(baseDirectory, DICTIONARY_SPELLING_DIRECTORY);
        if (recursiveDeleteFails(spellingDirectory)) {
            return Status.E_DEPLOY_DELETE_SPELLING_DIR;
        }
        File newIndexDirectory = new File(
                baseDirectory, DICTIONARY_INDEX_DIRECTORY + suffix);
        File newSpellingDirectory = new File(
                baseDirectory, DICTIONARY_SPELLING_DIRECTORY + suffix);
        if (!newIndexDirectory.renameTo(indexDirectory)) {
            return Status.E_DEPLOY_RENAME_INDEX_DIR;
        }
        if (!newSpellingDirectory.renameTo(spellingDirectory)) {
            return Status.E_DEPLOY_RENAME_SPELLING_DIR;
        }

        return Status.OK;

    }

    private static boolean recursiveDeleteFails(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    return true;
                }
            } else {
                return !file.delete();
            }
        }
        return false;
    }

}
