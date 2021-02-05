package com.dean.practice.gateway.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dean
 * @date 2020-11-27
 */
@UtilityClass
public class PathUtils {

    private static final String SLASH_STRING = "/";
    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();


    public String pathIgnorePartBySlash(String path, int partBySlash) {
        return SLASH_STRING
                + Arrays.stream(StringUtils.tokenizeToStringArray(path, SLASH_STRING))
                .skip(partBySlash).collect(Collectors.joining(SLASH_STRING));
    }


    public String pathIgnorePartBySlash(String path) {
        return pathIgnorePartBySlash(path, 1);
    }

    public boolean matchPath(String requestUri, String... patterns) {
        return Stream.of(patterns).anyMatch(pattern -> PATH_MATCHER.match(pattern, requestUri));
    }
}
