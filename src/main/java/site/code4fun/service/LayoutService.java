package site.code4fun.service;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import site.code4fun.ApplicationProperties;
import site.code4fun.constant.LayoutType;
import site.code4fun.constant.ProductCardLayout;
import site.code4fun.constant.Status;
import site.code4fun.model.AppSettingEntity;
import site.code4fun.model.Attachment;
import site.code4fun.model.BannerEntity;
import site.code4fun.model.LayoutTypeEntity;
import site.code4fun.model.dto.*;
import site.code4fun.repository.jpa.AppSettingRepository;
import site.code4fun.repository.jpa.LayoutTypeRepository;

import java.util.*;

import static site.code4fun.constant.AppConstants.UI_CONFIG_KEY;

@Service
@RequiredArgsConstructor
@Lazy
public class LayoutService extends AbstractBaseService<LayoutTypeEntity, Long> {
    @Getter(AccessLevel.PROTECTED)
    private final LayoutTypeRepository repository;
    private final AppSettingRepository settingRepository;
    private final Gson gson;
    private final ApplicationProperties applicationProperties;

    public LayoutTypeEntity findBySlug(String slug){
        return getRepository().findBySlug(slug).orElse(getRepository().findAll().get(0));
    }

    public List<LayoutTypeEntity> getAllActive(){
        List<LayoutTypeEntity> lst = getRepository().findAllByStatus(Status.ACTIVE);
        if (lst.isEmpty()){
            return getRepository().saveAll(simpleLayout());
        }
        return lst;
    }

    public LayoutTypeEntity changeStatus(Long id){
        LayoutTypeEntity entity = getById(id);
        entity.setStatus(entity.getStatus() == Status.ACTIVE ? Status.DRAFT : Status.ACTIVE);
        return entity;
    }

    @NotNull
    private List<LayoutTypeEntity> simpleLayout(){
        List<LayoutTypeEntity> lst = new ArrayList<>();

        Attachment image = new Attachment();
        image.setLink("https://pickbazarlaravel.s3.ap-southeast-1.amazonaws.com/905/bakery.jpg");
        image.setThumbnail("https://pickbazarlaravel.s3.ap-southeast-1.amazonaws.com/905/conversions/bakery-thumbnail.jpg");
        image.setName(FilenameUtils.getName(image.getLink()));

        Attachment image1 = new Attachment();
        image1.setLink("https://pickbazarlaravel.s3.ap-southeast-1.amazonaws.com/905/bakery.jpg");
        image1.setThumbnail("https://pickbazarlaravel.s3.ap-southeast-1.amazonaws.com/905/conversions/bakery-thumbnail.jpg");
        image1.setName(FilenameUtils.getName(image1.getLink()));

        List<BannerEntity> lstBanner = new ArrayList<>();
        BannerEntity banner = new BannerEntity();
        banner.setTitle("Get Your Bakery Items Delivered");
        banner.setDescription("Get your favorite bakery items baked and delivered to your doorsteps at any time");
        banner.setImage(image);
        lstBanner.add(banner);

        BannerEntity banner1 = new BannerEntity();
        banner1.setTitle("Your banner 1");
        banner1.setDescription("Get your favorite bakery items baked and delivered to your doorsteps first");
        banner1.setImage(image1);
        lstBanner.add(banner1);


        LayoutTypeEntity typeDTO = new LayoutTypeEntity();
        typeDTO.setSlug("grocery");
        typeDTO.setName("Grocery");
        typeDTO.setLanguage("en");
        typeDTO.setIcon("FruitsVegetable");
        typeDTO.setBanners(lstBanner);
        typeDTO.setProductCard(ProductCardLayout.helium);
        typeDTO.setLayoutType(LayoutType.classic);
        typeDTO.setStatus(Status.ACTIVE);
        lst.add(typeDTO);
        return lst;
    }

    public ShopConfigDTO saveSettings(ShopConfigDTO dto){
        Optional<AppSettingEntity> opts = settingRepository.findFirstByKey(UI_CONFIG_KEY);
        if (opts.isPresent()){
            opts.get().setValue(gson.toJson(dto));
        }else {
            AppSettingEntity entity = new AppSettingEntity();
            entity.setKey(UI_CONFIG_KEY);
            entity.setValue(gson.toJson(dto));
            settingRepository.save(entity);
        }
        return dto;
    }

    public ShopConfigDTO getSetting() {
        Optional<AppSettingEntity> opts = settingRepository.findFirstByKey(UI_CONFIG_KEY);
        ShopConfigDTO dto = new ShopConfigDTO();

        if (opts.isEmpty()){
            AttachmentDTO logo = new AttachmentDTO();
            logo.setLink("https://pickbazarlaravel.s3.ap-southeast-1.amazonaws.com/905/bakery.jpg");

            dto.setSiteTitle("TrungTQ");
            dto.setLogo(logo);
            Location location = new Location();
            location.setCountry("VN");
            location.setCity("HCM");
            location.setFormattedAddress("Ho Chi Minh, Viet nam");

            Social fb = new Social();
            fb.setUrl("https://facebook.com");
            fb.setIcon(Social.Icon.FacebookIcon);

            Social insta = new Social();
            insta.setUrl("https://www.instagram.com");
            insta.setIcon(Social.Icon.InstagramIcon);

            ContactDetails contactDetails = new ContactDetails();
            contactDetails.setContact("+841111111111");
            contactDetails.setWebsite(applicationProperties.getShopDomain());
            contactDetails.setEmailAddress("trungtrandb@gmail.com");
            contactDetails.setLocation(location);
            contactDetails.setSocials(Arrays.asList(fb, insta));

            dto.setContactDetails(contactDetails);
            dto.setDeliveryTime(getDeliveryTime());
            dto.setCopyrightText("Copyright © TrungTQ. All rights reserved.");
            dto.setExternalLink(applicationProperties.getShopDomain());
            dto.setExternalText("TrungTQ");
            dto.setMaintenance(getMaintenance());
            dto.setUnderMaintenance(false);

        }else {
            String value = opts.get().getValue();
            dto = gson.fromJson(value, ShopConfigDTO.class);
        }
        return dto;

    }

    @NotNull
    private Maintenance getMaintenance(){
        Maintenance dto = new Maintenance();
        dto.setStart(new Date());
        dto.setUntil(new Date());
        dto.setDescription("We are currently undergoing essential maintenance to elevate your browsing experience. Our team is working diligently to implement improvements that will bring you an even more seamless and enjoyable interaction with our site. During this period, you may experience temporary inconveniences. We appreciate your patience and understanding. Thank you for being a part of our community, and we look forward to unveiling the enhanced features and content soon.");
        dto.setTitle("Site is under Maintenance");
        return dto;
    }

    @NotNull
    private List<DeliveryTimeDTO> getDeliveryTime(){
        List<DeliveryTimeDTO> lst = new ArrayList<>();
        DeliveryTimeDTO dto = new DeliveryTimeDTO();
        dto.setTitle("Morning");
        dto.setDescription("8.00 AM - 11.00 AM");
        lst.add(dto);

        DeliveryTimeDTO dto2 = new DeliveryTimeDTO();
        dto2.setTitle("Noon");
        dto2.setDescription("11.00 AM - 2.00 PM");
        lst.add(dto2);

        DeliveryTimeDTO dto3 = new DeliveryTimeDTO();
        dto3.setTitle("Afternoon");
        dto3.setDescription("2.00 PM - 5.00 PM");
        lst.add(dto3);

        DeliveryTimeDTO dto4 = new DeliveryTimeDTO();
        dto4.setTitle("Express Delivery");
        dto4.setDescription("In 2 hours");
        lst.add(dto4);
        return lst;
    }
}
