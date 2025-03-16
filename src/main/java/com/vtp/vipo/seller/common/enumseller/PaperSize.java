package com.vtp.vipo.seller.common.enumseller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum PaperSize {
    A5("A5"),
    A6("A6"),
    A7("A7");

    final String size;

    // Create Map for PaperSize and Copies
    public static Map<String, String> createLabelMap() {
        Map<String, String> labelMap = new HashMap<>();

        for (PaperSize paperSize : PaperSize.values()) {
            for (Copies copies : Copies.values()) {
                String key = paperSize.getSize() + "_" + copies.getValue();
                String url = generateUrl(paperSize, copies);
                labelMap.put(key, url);
            }
        }

        return labelMap;
    }

    // Create URL for PaperSize and Copies
    private static String generateUrl(PaperSize paperSize, Copies copies) {
        return switch (paperSize) {
            case A5 -> generateUrlForA5(copies);
            case A6 -> generateUrlForA6(copies);
            case A7 -> generateUrlForA7(copies);
        };
    }

    // Create URL for A5
    private static String generateUrlForA5(Copies copies) {
        if (copies == Copies.ONE_SHEET) {
            return "/DigitalizePrint/report.do?type=1&bill=${code}=&showPostage=1&printCopy=1&showPhone=false&showProductContent=true";
        } else {
            return "/DigitalizePrint/report.do?type=1&bill=${code}=&showPostage=1&printCopy=2&showPhone=false&showProductContent=true";
        }
    }

    // Create URL for A6
    private static String generateUrlForA6(Copies copies) {
        if (copies == Copies.ONE_SHEET) {
            return "/DigitalizePrint/report.do?type=2&bill=${code}=&showPostage=1&printCopy=1&showPhone=false&showProductContent=true";
        } else {
            return "/DigitalizePrint/report.do?type=2&bill=${code}=&showPostage=1&printCopy=2&showPhone=false&showProductContent=true";
        }
    }

    // Create URL for A7
    private static String generateUrlForA7(Copies copies) {
        if (copies == Copies.ONE_SHEET) {
            return "/DigitalizePrint/report.do?type=100&bill=${code}=&showPostage=1&printCopy=1&showPhone=false&showProductContent=true";
        } else {
            return "/DigitalizePrint/report.do?type=100&bill=${code}=&showPostage=1&printCopy=2&showPhone=false&showProductContent=true";
        }
    }

    // Get key from PaperSize and Copies
    public static String getKey(PaperSize paperSize, Copies copies) {
        String key = paperSize.getSize() + "_" + copies.getValue();
        Map<String, String> labelMap = createLabelMap();
        if (labelMap.containsKey(key)) {
            return key;
        }
        return null;
    }

    // Get value from PaperSize and Copies
    public static String getValue(PaperSize paperSize, Copies copies) {
        String key = paperSize.getSize() + "_" + copies.getValue();
        Map<String, String> labelMap = createLabelMap();
        return labelMap.get(key);
    }

    // Get value from key
    public static String getValue(String key) {
        Map<String, String> labelMap = createLabelMap();
        return labelMap.get(key);
    }

    public static boolean contains(String name) {
        for (PaperSize c : PaperSize.values()) {
            if (c.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
