package site.code4fun.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import site.code4fun.model.CampaignEntity;
import site.code4fun.repository.jpa.CampaignRepository;
import site.code4fun.service.email.MailChimp;

import java.util.ArrayList;
import java.util.List;


@Service
@Lazy
@RequiredArgsConstructor
public class CampaignService extends AbstractBaseService<CampaignEntity, Long> {
    @Getter(AccessLevel.PROTECTED)
    private final CampaignRepository repository;
    private final MailChimp mailChimp;

    public  List<CampaignEntity> sync() {
        List<CampaignEntity> lstCampaign = new ArrayList<>();
        mailChimp.getAllCampaign().forEach(campaign -> {
            CampaignEntity c = new CampaignEntity();
            c.setProviderId(campaign.getId());
            c.setStatus(campaign.getStatus());
            c.setContentType(campaign.getContentType());
            c.setEmailsSent(campaign.getEmailsSent());
            if (campaign.getSettings() != null){
                c.setTitle(campaign.getSettings().getTitle());
                c.setFromName(campaign.getSettings().getFromName());
                c.setToName(campaign.getSettings().getToName());
                c.setReplyTo(campaign.getSettings().getReplyTo());
                c.setName(campaign.getSettings().getSubjectLine());
                c.setTemplateId(campaign.getSettings().getTemplateId());
                c.setUseConversation(campaign.getSettings().getUseConversation());
            }
            lstCampaign.add(c);
        });
        getRepository().saveAll(lstCampaign);
        return lstCampaign;
    }
}