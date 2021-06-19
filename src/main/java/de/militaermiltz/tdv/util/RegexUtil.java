package de.militaermiltz.tdv.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

    public static List<String> getMatches(String regex, String input){
        final List<String> allMatches = new ArrayList<>();
        Matcher m = Pattern.compile(regex).matcher(input);

        while (m.find()){
            allMatches.add(m.group());
        }
        return allMatches;
    }
}
