package site.code4fun.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("unused")
public final class AppEndpoints {
    public static final String AUTH_ENDPOINT = "/auth";
    public static final String CATEGORIES_ENDPOINT = "/categories";
    public static final String ATTENDANCES_ENDPOINT = "/attendances";
    public static final String STOCK_ENDPOINT = "/stock";
    public static final String LEAVE_REQUESTS_ENDPOINT = "/leave-requests";
    public static final String SUPPLIER_ENDPOINT = "/suppliers";
    public static final String LEAVE_TYPE_ENDPOINT = "/leave-types";
    public static final String INVENTORIES_ENDPOINT = "/inventories";
    public static final String TAGS_ENDPOINT = "/tags";
    public static final String INTEGRATIONS_ENDPOINT = "/integrations";
    public static final String FILES_ENDPOINT = "/files";
    public static final String FACES_ENDPOINT = "/faces";
    public static final String PRODUCTS_ENDPOINT = "/products";
    public static final String POSTS_ENDPOINT = "/posts";
    public static final String USERS_ENDPOINT = "/users";
    public static final String ORDERS_ENDPOINT = "/orders";
    public static final String SLIDES_ENDPOINT = "/slides";
    public static final String HEALTH_CHECK_ENDPOINT = "/health";
    public static final String CAMPAIGNS_ENDPOINT = "/campaigns";
    public static final String CONTACTS_ENDPOINT = "/contacts";
    public static final String SHIFTS_ENDPOINT = "/shifts";


    // ADMIN ENDPOINT
    public static final String ADMIN_API_PREFIX = "/admin";
    // END ADMIN ENDPOINT

}
