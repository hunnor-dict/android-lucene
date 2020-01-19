package net.hunnor.dict.android.service;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class StorageServiceTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testFileStats() throws IOException {

        File folder1 = temporaryFolder.newFolder("folder-1");
        File file11 = new File(folder1, "file-1-1.txt");
        FileUtils.writeByteArrayToFile(file11, "foo".getBytes());
        File file12 = new File(folder1, "file-1-2.txt");
        FileUtils.writeByteArrayToFile(file12, "bar".getBytes());

        File folder2 = temporaryFolder.newFolder("folder-2");
        File file21 = new File(folder2, "file-2-1.txt");
        FileUtils.writeByteArrayToFile(file21, "baz qux".getBytes());

        StorageService storageService = new StorageService();
        Map<String, Long> stats = storageService.fileStats(folder1, folder2);

        Long date = stats.get(StorageService.DATE);
        assertNotNull(date);
        assertTrue(date > 0);

        Long size = stats.get(StorageService.SIZE);
        assertNotNull(size);
        assertEquals(13, (long) size);

    }

    @Test
    public void testDeleteRecursively() throws IOException {

        File directory = temporaryFolder.newFolder("folder");
        File file = new File(directory, "file.txt");

        StorageService storageService = new StorageService();
        storageService.deleteRecursively(directory);

        String[] files = temporaryFolder.getRoot().list();
        assertNotNull(files);
        assertEquals(0, files.length);

    }

    @Test
    public void testExtract() throws IOException {

        File file = temporaryFolder.newFile("file.zip");
        OutputStream stream = new FileOutputStream(file);
        ZipOutputStream zipStream = new ZipOutputStream(stream);

        ZipEntry entry1 = new ZipEntry("hunnor-lucene-index/file-1.txt");
        zipStream.putNextEntry(entry1);
        zipStream.write("foo".getBytes());
        zipStream.closeEntry();

        ZipEntry entry2 = new ZipEntry("hunnor-lucene-spelling/file-2.txt");
        zipStream.putNextEntry(entry2);
        zipStream.write("bar".getBytes());
        zipStream.closeEntry();

        zipStream.close();

        assertTrue(file.isFile());
        assertTrue(file.length() > 6);

        InputStream inputStream = new FileInputStream(file);

        StorageService storageService = new StorageService();
        storageService.extract(inputStream, temporaryFolder.getRoot(), "-dir");

        File indexDir = new File(temporaryFolder.getRoot(), "hunnor-lucene-index-dir");
        assertTrue(indexDir.isDirectory());
        File indexDirFile = new File(indexDir, "file-1.txt");
        assertTrue(indexDirFile.isFile());
        assertEquals(3, indexDirFile.length());

        File spellingDir = new File(temporaryFolder.getRoot(), "hunnor-lucene-spelling-dir");
        assertTrue(spellingDir.isDirectory());
        File spellingDirFile = new File(spellingDir, "file-2.txt");
        assertTrue(spellingDirFile.isFile());
        assertEquals(3, spellingDirFile.length());

    }

    @Test
    public void testRename() throws IOException {

        File from = temporaryFolder.newFolder("from");
        File to = new File(temporaryFolder.getRoot(), "to");

        StorageService storageService = new StorageService();
        storageService.rename(from, to);

        String[] files = temporaryFolder.getRoot().list();
        assertNotNull(files);
        assertEquals(1, files.length);
        assertEquals("to", files[0]);

    }

}
