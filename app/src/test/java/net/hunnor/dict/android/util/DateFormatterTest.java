package net.hunnor.dict.android.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DateFormatterTest {

    @Test
    public void testFormatDate() {
        long timestamp = 1262434455;
        assertEquals("2010-01-02 13:14:15",
                DateFormatter.formatDate(timestamp * 1000));
    }

    @Test
    public void testReformatDate() throws DateFormatterException {
        assertEquals("2010-01-02 13:14:15",
                DateFormatter.reformatDate("Sat, 02 Jan 2010 12:14:15 GMT"));
    }

    @Test(expected = DateFormatterException.class)
    public void testReformatDateError() throws DateFormatterException {
        DateFormatter.reformatDate("android");
    }

}
