package site.code4fun.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.code4fun.constant.SearchOperator;
import site.code4fun.model.dto.SearchCriteria;

import java.lang.reflect.Field;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;
import static site.code4fun.constant.AppConstants.COMMA;

/**
 * Utility class for parsing and processing URLs and search criteria
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class UrlParserUtils {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    /**
     * Parses query string parameters into search criteria
     * @param mapRequestParam Map of request parameters
     * @param clazz Target class for field validation
     * @return List of search criteria
     */
    public static List<SearchCriteria> parserQueryString(Map<String, String> mapRequestParam, Class<?> clazz) {
        List<SearchCriteria> res = new ArrayList<>();
        if (!clazzAndRequestNotNull(clazz, mapRequestParam)) {
            log.warn("Invalid input parameters: clazz={}, mapRequestParam={}", clazz, mapRequestParam);
            return res;
        }

        List<String> fieldNames = new ArrayList<>(Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .toList());
        fieldNames.add("created");
        fieldNames.add("updated");

        mapRequestParam.forEach((searchKey, searchValue) -> {
            try {
                processExcludeIds(mapRequestParam, res);
                if (valueAndFieldNameNotNull(searchValue, searchKey, fieldNames)) {
                    processSearchValue(searchKey, searchValue, clazz, res);
                }
            } catch (Exception ex) {
                log.error("Error processing search parameter: key={}, value={}", searchKey, searchValue, ex);
            }
        });
        return res;
    }

    /**
     * Builds a pretty URL from input string
     * @param input Input string to normalize
     * @return Normalized URL string
     */
    public static String buildPrettyURL(final String input) {
        if (isBlank(input)) return EMPTY;
        return RegexUtils.normalize(input).toLowerCase(Locale.ENGLISH);
    }

    /**
     * Extracts Facebook ID from profile URL
     * @param link Facebook profile URL
     * @return Facebook ID
     */
    public static String getFbId(String link) {
        if (isBlank(link)) {
            return "";
        }
        try {
            if (link.contains("profile.php")) {
                return link.trim().split("=")[1];
            }
            String[] arr = link.split("/");
            return arr[arr.length - 1];
        } catch (Exception e) {
            log.error("Error extracting Facebook ID from URL: {}", link, e);
            return "";
        }
    }

    /**
     * Gets client IP address from request
     * @param request HTTP request
     * @return Client IP address
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                if (header.equals("X-Forwarded-For")) {
                    String[] ips = ip.split(",");
                    log.info("X-Forwarded-For: {}", ip);
                    return ips[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }

    private static void processExcludeIds(Map<String, String> mapRequestParam, List<SearchCriteria> res) {
        String excludeIds = mapRequestParam.get("excludeIds");
        if (isNotBlank(excludeIds)) {
            res.add(new SearchCriteria("id", SearchOperator.NOT_IN, excludeIds));
        }
    }

    private static void processSearchValue(String searchKey, String searchValue, Class<?> clazz, List<SearchCriteria> res) {
        if (searchValue.contains(COMMA)) {
            res.addAll(buildSearchWithComma(searchKey, searchValue, clazz));
        } else {
            res.add(new SearchCriteria(searchKey, SearchOperator.EQUAL, searchValue));
        }
    }

    private static boolean clazzAndRequestNotNull(Class<?> clazz, Map<String, String> mapRequestParam) {
        return clazz != null && mapRequestParam != null && !mapRequestParam.isEmpty();
    }

    private static List<SearchCriteria> buildMinMaxSearch(String v, String k){
        String[] arrVal = v.split(COMMA);
        List<SearchCriteria> res = new ArrayList<>();
        try{
            if (isNotBlank(arrVal[0])){
                SearchCriteria min = new SearchCriteria(k, SearchOperator.GREATER_THAN, arrVal[0]);
                res.add(min);
            }

            if (isNotBlank(arrVal[1])){
                SearchCriteria max = new SearchCriteria(k, SearchOperator.LESS_THAN, arrVal[1]);
                res.add(max);
            }
        }catch (Exception e){
            log.warn(e.getMessage());
        }
        return res;
    }

    private static boolean valueAndFieldNameNotNull(String v, String k, List<String> fieldNames) {
        return isNotEmpty(v) && fieldNames.contains(k);
    }

    public static boolean isCollectionField(Class<?> fieldType){
        return fieldType != null && Collection.class.isAssignableFrom(fieldType);
    }

    private static List<SearchCriteria> buildSearchWithComma(String searchKey, String searchValue, Class<?> clazz){
        List<SearchCriteria> res = new ArrayList<>();
        try{
            if(!isCollectionField(clazz.getDeclaredField(searchKey).getType())){
                res.addAll(buildMinMaxSearch(searchValue, searchKey));
            }else{
                res.add(new SearchCriteria(searchKey, SearchOperator.IN, searchValue));
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return res;
    }
}
