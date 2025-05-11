package site.code4fun.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.NONE)
public class CollectionUtils {
    public static boolean containsIgnoreCase(List<String> collection, String string){
        return collection.stream().anyMatch(x -> x.equalsIgnoreCase(string));
    }
}
