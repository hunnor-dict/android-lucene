package net.hunnor.dict.android.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {

    private static final String TAG = DateFormatter.class.getName();

    public static String formatDate(long timestamp) {
        return formatDate(new Date(timestamp));
    }

    private static String formatDate(Date date) {
        SimpleDateFormat appFormat =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return appFormat.format(date);
    }

    public static String reformatDate(String original) throws DateFormatterException {
        SimpleDateFormat httpHeaderFormat =
                new SimpleDateFormat("EEE, dd MMM yyyy H:m:s zzz", Locale.ENGLISH);
        String result = null;
        try {
            Date date = httpHeaderFormat.parse(original);
            result = formatDate(date);
        } catch (ParseException e) {
            throw new DateFormatterException(e);
        }
        return result;
    }

}
