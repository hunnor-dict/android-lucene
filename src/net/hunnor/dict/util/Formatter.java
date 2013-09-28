package net.hunnor.dict.util;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Formatter {

	public static String humanReadableBytes(double bytes) {
		String[] units = {"Byte", "kB", "MB", "GB"};
		int i = 0;
		double result = bytes;
		while (i < units.length && result > 1024) {
			result = result / 1024;
			i++;
		}
		DecimalFormat decimalFormat = new DecimalFormat("#.#");
		return decimalFormat.format(result) + " " + units[i];
	}

	public static String date(long timestamp) {
		return date(timestamp, "yyyy-MM-dd");
	}

	public static String date(long timestamp, String pattern) {
		Date date = new Date(timestamp);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.US);
		return simpleDateFormat.format(date);
	}

	public static String date(String date, String inputPattern) {
		return date(date, inputPattern, "yyyy-MM-dd");
	}

	public static String date(String date, String inputPattern, String outputPattern) {
		SimpleDateFormat inputFormat =
				new SimpleDateFormat(inputPattern, Locale.US);
		SimpleDateFormat outputFormat =
				new SimpleDateFormat(outputPattern, Locale.US);
		try {
			Date parsedDate = inputFormat.parse(date);
			return outputFormat.format(parsedDate);
		} catch (ParseException exception) {
			return null;
		}
	}

}
