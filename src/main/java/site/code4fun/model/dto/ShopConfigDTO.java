package site.code4fun.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class ShopConfigDTO implements Serializable {
    private List<DeliveryTimeDTO> deliveryTime;
    private boolean isProductReview;
    private boolean useGoogleMap;
    private boolean enableTerms;
    private boolean enableCoupons;
    private boolean enableReviewPopup;
    private String defaultPaymentGateway;
//    private ReviewSystem reviewSystem;
    private SeoDTO seo;
    private AttachmentDTO logo;
    private AttachmentDTO collapseLogo;
    private boolean useOtp;
    private String currency;
    private String taxClass;
    private String siteTitle;
    private boolean freeShipping;
    private int signupPoints;
    private String siteSubtitle;
    private String shippingClass;
    private ContactDetails contactDetails;
    private Map<String, String> paymentGateway;
//    private CurrencyOptions currencyOptions;
    private boolean useEnableGateway;
    private boolean useCashOnDelivery;
    private int freeShippingAmount;
    private int minimumOrderAmount;
    private boolean useMustVerifyEmail;
    private int maximumQuestionLimit;
    private int currencyToWalletRatio;
    @JsonProperty("StripeCardOnly")
    private boolean stripeCardOnly;
    private boolean guestCheckout;
    @JsonProperty("server_info")
    private ServerInfo serverInfo;
    private boolean useAi;
    private String defaultAi;
    private Object maxShopDistance;
    private String siteLink;
    private String copyrightText;
    private String externalText;
    private String externalLink;
//    private SmsEvent smsEvent;
//    private EmailEvent emailEvent;
//    private PushNotification pushNotification;

    @JsonProperty("isUnderMaintenance")
    private boolean isUnderMaintenance;
    private Maintenance maintenance;

    @JsonProperty("isPromoPopUp")
    private boolean isPromoPopUp;
    private PromoPopup promoPopup;
}

@Data
class PopUpNotShow implements Serializable{
    private String title;
    private int popUpExpiredIn;
}

@Data
class PromoPopup implements Serializable{
    private AttachmentDTO image;
    private String title;
    private int popUpDelay;
    private String description;
    private PopUpNotShow popUpNotShow;
    private String popUpExpiredIn;
}