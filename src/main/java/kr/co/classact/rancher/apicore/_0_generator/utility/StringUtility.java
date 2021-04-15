package kr.co.classact.rancher.apicore._0_generator.utility;

import java.util.List;
import java.util.function.Function;

public final class StringUtility {

    private static final StringUtility instance = new StringUtility();
    public static StringUtility ins() { return instance; }
    private StringUtility() { }

    public final Function<String, String> capitalize =
            (input) ->
                    /*    if */ input.length() == 1 ?
                    /*  true */ input.toUpperCase() :
                    /* false */ (String.valueOf(input.charAt(0)).toUpperCase()) + input.substring(1);

    public final Function<String, String> toLowerCamelCase =
            (input) -> String.valueOf(input.charAt(0)).toLowerCase() + input.substring(1);

    public final Function<String, String> addUnderscore =
            (input) -> input.replaceAll("([a-z])([A-Z]+)", "$1_$2");

    public final Function<String, String> toUnderscore =
            (input) -> input.replaceAll("-", "_");

    public final Function<String, String> upper
            = String::toUpperCase;

    public String trimPrefix(String input, String prefix) {
        return input.startsWith(prefix) ? input.substring(prefix.length()) : input;
    }

    public String trimSuffix(String input, String suffix) {
        return input.endsWith(suffix) ? input.substring(0, input.lastIndexOf(suffix)) : input;
    }

    public boolean startsWith(String target, List<String> withs) {
        return startsWith(target, withs.toArray(new String[0]));
    }

    public boolean startsWith(String target, String... withs) {
        for (String with : withs) {
            if (target.startsWith(with)) {
                return true;
            }
        }
        return false;
    }
}
