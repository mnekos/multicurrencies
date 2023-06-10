package pl.mnekos.multicurrencies.commands;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TabCompleteUtils {

    public static List<String> getComplements(String prefix, Stream<String> possibleArguments, boolean ignoreCase) {
        if(ignoreCase) {
            prefix = prefix.toLowerCase();
            possibleArguments = possibleArguments.map(String::toLowerCase);
        }

        return getComplements0(prefix, possibleArguments);
    }

    public static List<String> getComplements0(String prefix, Stream<String> possibleArguments) {
        return possibleArguments.filter(arg -> arg.startsWith(prefix)).collect(Collectors.toList());
    }

}