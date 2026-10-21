package io.github.violetpixel.bookstore.common;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Normalização de texto: slugs e a coluna de busca sem acentos. */
public final class Text {

    private Text() { }

    public static String fold(String value) {
        if (value == null) {
            return "";
        }
        String noAccents = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return noAccents.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    public static String slug(String value) {
        return fold(value).replace(' ', '-');
    }

    public static String searchable(String... parts) {
        String joined = Stream.of(parts).filter(Objects::nonNull).map(Text::fold).filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));
        return joined.length() > 1000 ? joined.substring(0, 1000) : joined;
    }

    public static String digits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
