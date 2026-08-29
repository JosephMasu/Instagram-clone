package com.masu.post_service.comment;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MentionParser {

    private static final Pattern MENTION = Pattern.compile(
            "(?<![A-Za-z0-9._])@([A-Za-z0-9._]{3,30})"
    );

    private MentionParser() {
    }

    public static Set<String> usernames(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }

        Set<String> usernames = new LinkedHashSet<>();
        Matcher matcher = MENTION.matcher(text);
        while (matcher.find()) {
            usernames.add(matcher.group(1));
        }
        return usernames;
    }
}
