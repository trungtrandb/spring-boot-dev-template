package site.code4fun.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.Normalizer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class RegexUtils {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern EDGE_SD_HASHES = Pattern.compile("(^-)|(-$)");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@(.+)$",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)?(www\\.)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(/\\S*)?$",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[0-9]{10,15}$"
    );
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    );

    /**
     * Kiểm tra tính hợp lệ của email
     * @param email Email cần kiểm tra
     * @return true nếu email hợp lệ
     */
    public static boolean isValidEmail(final String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Kiểm tra tính hợp lệ của URL
     * @param url URL cần kiểm tra
     * @return true nếu URL hợp lệ
     */
    public static boolean isValidUrl(final String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }

    /**
     * Kiểm tra tính hợp lệ của số điện thoại
     * @param phone Số điện thoại cần kiểm tra
     * @return true nếu số điện thoại hợp lệ
     */
    public static boolean isValidPhone(final String phone) {
        if (StringUtils.isBlank(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Kiểm tra tính hợp lệ của mật khẩu
     * @param password Mật khẩu cần kiểm tra
     * @return true nếu mật khẩu hợp lệ
     */
    public static boolean isValidPassword(final String password) {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Trích xuất host từ URL
     * @param input URL đầu vào
     * @return Host đã được trích xuất
     */
    public static String parseHost(final String input) {
        if (StringUtils.isBlank(input)) {
            return "";
        }
        if (input.contains("localhost")) {
            return "localhost";
        }
        try {
            return input.replaceAll("http(s)?://|www\\.|/.*", "");
        } catch (Exception e) {
            log.error("Error parsing host from input: {}", input, e);
            return "";
        }
    }

    /**
     * Chuẩn hóa chuỗi thành slug
     * @param input Chuỗi đầu vào
     * @return Chuỗi đã được chuẩn hóa
     */
    public static String normalize(final String input) {
        if (StringUtils.isBlank(input)) {
            return "";
        }
        try {
            String noWhitespace = WHITESPACE.matcher(input).replaceAll("-");
            String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
            String slug = NON_LATIN.matcher(normalized).replaceAll("");
            return EDGE_SD_HASHES.matcher(slug).replaceAll("");
        } catch (Exception e) {
            log.error("Error normalizing input: {}", input, e);
            return "";
        }
    }

    /**
     * Trích xuất số từ chuỗi
     * @param input Chuỗi đầu vào
     * @return Chuỗi chỉ chứa số
     */
    public static String extractNumbers(final String input) {
        if (StringUtils.isBlank(input)) {
            return "";
        }
        return input.replaceAll("[^0-9]", "");
    }

    /**
     * Trích xuất chữ cái từ chuỗi
     * @param input Chuỗi đầu vào
     * @return Chuỗi chỉ chứa chữ cái
     */
    public static String extractLetters(final String input) {
        if (StringUtils.isBlank(input)) {
            return "";
        }
        return input.replaceAll("[^a-zA-Z]", "");
    }

    /**
     * Kiểm tra xem chuỗi có chứa ký tự đặc biệt không
     * @param input Chuỗi đầu vào
     * @return true nếu chuỗi chứa ký tự đặc biệt
     */
    public static boolean containsSpecialCharacters(final String input) {
        if (StringUtils.isBlank(input)) {
            return false;
        }
        return input.matches(".*[^a-zA-Z0-9\\s].*");
    }

    /**
     * Xóa các ký tự đặc biệt từ chuỗi
     * @param input Chuỗi đầu vào
     * @return Chuỗi đã được xóa ký tự đặc biệt
     */
    public static String removeSpecialCharacters(final String input) {
        if (StringUtils.isBlank(input)) {
            return "";
        }
        return input.replaceAll("[^a-zA-Z0-9\\s]", "");
    }
}
