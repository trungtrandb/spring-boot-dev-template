package site.code4fun.service.google;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import site.code4fun.exception.ValidationException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Service
@Slf4j
@RequiredArgsConstructor
@Lazy
public class GoogleTranslateService {

    private static final String[] LANGUAGES = {
            "ab", "ace", "ach", "af", "sq", "alz", "am", "ar", "hy", "as",
            "awa", "ay", "az", "ban", "bm", "ba", "eu", "btx", "bts", "bbc",
            "be", "bem", "bn", "bew", "bho", "bik", "bs", "br", "bg", "bua",
            "yue", "ca", "ceb", "ny", "zh-CN", "zh-TW", "cv", "co", "crh",
            "hr", "cs", "da", "din", "dv", "doi", "dov", "nl", "dz", "en",
            "eo", "et", "ee", "fj", "fil", "tl", "fi", "fr", "fr-FR", "fr-CA",
            "fy", "ff", "gaa", "gl", "lg", "ka", "de", "el", "gn", "gu", "ht",
            "cnh", "ha", "haw", "iw", "he", "hil", "hi", "hmn", "hu", "hrx",
            "is", "ig", "ilo", "id", "ga", "it", "ja", "jw", "jv", "kn", "pam",
            "kk", "km", "cgg", "rw", "ktu", "gom", "ko", "kri", "ku", "ckb",
            "ky", "lo", "ltg", "la", "lv", "lij", "li", "ln", "lt", "lmo", "luo",
            "lb", "mk", "mai", "mak", "mg", "ms", "ms-Arab", "ml", "mt", "mi",
            "mr", "chm", "mni-Mtei", "min", "lus", "mn", "my", "nr", "new", "ne",
            "nso", "no", "nus", "oc", "or", "om", "pag", "pap", "ps", "fa", "pl",
            "pt", "pt-PT", "pt-BR", "pa", "pa-Arab", "qu", "rom", "ro", "rn", "ru",
            "sm", "sg", "sa", "gd", "sr", "st", "crs", "shn", "sn", "scn", "szl",
            "sd", "si", "sk", "sl", "so", "es", "su", "sw", "ss", "sv", "tg", "ta",
            "tt", "te", "tet", "th", "ti", "ts", "tn", "tr", "tk", "ak", "uk", "ur",
            "ug", "uz", "vi", "cy", "xh", "yi", "yo", "yua", "zu"
    };

    private static final Set<String> SUPPORTED_LANG = new HashSet<>(Arrays.asList(LANGUAGES));

    private final GoogleServiceAccountProperties config;
    private Translate translateService;

    private Translate getService() {
        if (translateService == null){
            translateService = TranslateOptions.newBuilder()
                    .setCredentials(config.getCredential())
                    .setProjectId(config.getProjectId()).build()
                    .getService();
        }
        return translateService;
    }

    @SneakyThrows
    public String translate(@Nullable String text, @Nullable String sourceLang, @Nullable String targetLang){
        if (isBlank(text)) return "";

        Translation translation;
        log.info("Translating '{}' from '{}' to '{}'", StringUtils.abbreviate(text, 20), sourceLang, targetLang);
        if (isNotEmpty(sourceLang) && isNotEmpty(targetLang)){
            checkValidLang(sourceLang, targetLang);
            translation = getService().translate(text,
                    Translate.TranslateOption.sourceLanguage(sourceLang),
                    Translate.TranslateOption.targetLanguage(targetLang));
        }else {
            translation = getService().translate(text);
        }
        return translation.getTranslatedText();
    }

    private void checkValidLang(String... langs){
        for (String lang: langs) {
            if(!SUPPORTED_LANG.contains(lang))
                throw new ValidationException("Un Support lang %s, valid value is %s ", lang, SUPPORTED_LANG);
        }
    }
}
