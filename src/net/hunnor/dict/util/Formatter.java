package net.hunnor.dict.util;

import java.text.DecimalFormat;

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

}
