package site.code4fun.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import site.code4fun.service.storage.GoogleCloudStore;

import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppConstants {
    public static final String TABLE_PREFIX = "tbl_";
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String DEFAULT_SORT_COLUMN = "id";
    public static final String DEFAULT_STORAGE_PROVIDER = GoogleCloudStore.class.getSimpleName();
    public static final List<String> SORT_LIST = Arrays.asList("ASC", "DESC"); //NOSONAR
    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    public static final String SEARCH_KEY = "query";
    public static final String LOCALE_RESOLVE_NAME = "language";
    public static final String JOB_PARAM_SUBJECT = "subject";
    public static final String JOB_PARAM_BODY = "body";
    public static final String JOB_PARAM_TYPE = "type";
    public static final String JOB_PARAM_RECIPIENTS = "recipients";
    public static final String COMMA = ",";
    public static final String TIME_ZONE = "GMT+07:00";
    public static final String NOT_IMPLEMENT = "Not implemented";
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

    // App Entity Config Key
    public static final String UI_CONFIG_KEY = "UI";
    public static final String STORAGE_PROVIDER_KEY = "StorageProvider";
  }
