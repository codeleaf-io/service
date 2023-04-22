package io.codeleaf.service.url;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PathLists {

    private PathLists() {
    }

    public static List<String> fromEncodedPath(String encodedPath) {
        if (encodedPath == null || encodedPath.isEmpty()) {
            return Collections.emptyList();
        }
        if (encodedPath.startsWith("/")) {
            encodedPath = encodedPath.substring(1);
        }
        List<String> basePath = new ArrayList<>();
        for (String part : encodedPath.split("/")) {
            basePath.add(URLDecoder.decode(part, StandardCharsets.UTF_8));
        }
        return basePath;
    }

    public static String toEncodedPath(List<String> basePath) {
        if (basePath == null) {
            return "";
        }
        if (basePath.isEmpty()) {
            return "/";
        }
        StringBuilder builder = new StringBuilder();
        for (String part : basePath) {
            builder.append('/').append(URLEncoder.encode(part, StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

}
