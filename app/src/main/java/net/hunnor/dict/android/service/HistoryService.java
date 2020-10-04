package net.hunnor.dict.android.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Helper class for managing search history as a single string.
 * The string is saved to SharedPreferences.
 */
public class HistoryService {

    private static final String HISTORY_WORDS_SEPARATOR = "|";

    public String append(String existingElements, String newElement, int maxLength) {

        List<String> existingList = readToList(existingElements);

        if (hasContent(newElement)) {
            List<String> newList = makeNewList(newElement);
            if (!existingList.isEmpty()) {
                existingList.remove(newElement);
                newList.addAll(existingList);
            }
            existingList = newList;
        }

        if (existingList.size() > maxLength) {
            existingList = limitListSize(existingList, maxLength);
        }

        return joinWithSeparator(existingList);

    }

    private boolean hasContent(String str) {
        return str != null && !str.isEmpty();
    }

    private List<String> makeNewList(String element) {
        List<String> result = new ArrayList<>();
        result.add(element);
        return result;
    }

    public List<String> readToList(String elementChain) {
        List<String> result = new ArrayList<>();
        if (hasContent(elementChain)) {
            String[] resultArray = elementChain.split(Pattern.quote(HISTORY_WORDS_SEPARATOR));
            result = new ArrayList<>(Arrays.asList(resultArray));
        }
        return result;
    }

    private List<String> limitListSize(List<String> list, int maxLength) {
        List<String> result = list;
        if (result.size() > maxLength) {
            result = result.subList(0, maxLength);
        }
        return result;
    }

    private String joinWithSeparator(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String element : list) {
            if (sb.length() > 0) {
                sb.append(HISTORY_WORDS_SEPARATOR);
            }
            sb.append(element);
        }
        return sb.toString();
    }

}
