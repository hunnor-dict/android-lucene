package net.hunnor.dict.android.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HistoryServiceTest {

    @Test
    public void testAppendNullToEmptyString() {
        HistoryService historyService = new HistoryService();
        String actual = historyService.append("", null, 10);
        assertEquals("", actual);
    }

    @Test
    public void testAppendEmptyStringToEmptyString() {
        HistoryService historyService = new HistoryService();
        String actual = historyService.append("", "", 10);
        assertEquals("", actual);
    }

    @Test
    public void testAppendNewValueToEmptyString() {
        HistoryService historyService = new HistoryService();
        String actual = historyService.append("", "foo", 10);
        assertEquals("foo", actual);
    }

    @Test
    public void testAppendNewValueToExistingValues() {
        HistoryService historyService = new HistoryService();
        String actual = historyService.append("foo|bar", "baz", 10);
        assertEquals("baz|foo|bar", actual);
    }

    @Test
    public void testAppendExistingValueToExistingValues() {
        HistoryService historyService = new HistoryService();
        String actual = historyService.append("foo|bar|baz", "bar", 10);
        assertEquals("bar|foo|baz", actual);
    }

    @Test
    public void testAppendNullToExistingValues() {
        HistoryService historyService = new HistoryService();
        String actual = historyService.append("foo|bar", null, 10);
        assertEquals("foo|bar", actual);
    }

    @Test
    public void testAppendEmptyStringToExistingValues() {
        HistoryService historyService = new HistoryService();
        String actual = historyService.append("foo|bar", "", 10);
        assertEquals("foo|bar", actual);
    }

    @Test
    public void testAppendNullLimitSize() {
        HistoryService historyService = new HistoryService();
        String actual = historyService.append("foo|bar|baz", null, 2);
        assertEquals("foo|bar", actual);
    }

    @Test
    public void testAppendNewValueLimitSize() {
        HistoryService historyService = new HistoryService();
        String actual = historyService.append("foo|bar", "baz", 2);
        assertEquals("baz|foo", actual);
    }

}
