package com.vtp.vipo.seller.common.utils;

import org.springframework.data.redis.connection.RedisNode;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtils {

    public static String getAlias(String originKeyWord) {
        String normalized = Normalizer.normalize(originKeyWord, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String removedDiacritics = pattern.matcher(normalized).replaceAll("");
        String alias = removedDiacritics.replaceAll("\\s+", "-").toLowerCase();
        return alias;
    }

    public static List<RedisNode> convertStringToRedisNodes(String nodes) {
        return Arrays.stream(nodes.split(", "))
                .map(nodeString -> {
                    String[] parts = nodeString.split(":");
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("Invalid node format: " + nodeString);
                    }
                    String host = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    return new RedisNode(host, port);
                })
                .collect(Collectors.toList());
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

}
