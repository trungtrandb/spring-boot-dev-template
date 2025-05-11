package site.code4fun.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import site.code4fun.constant.QueueName;
import site.code4fun.constant.SearchOperator;
import site.code4fun.model.Address;
import site.code4fun.model.ContactEntity;
import site.code4fun.model.dto.SearchCriteria;
import site.code4fun.model.dto.SendMailDTO;
import site.code4fun.model.request.SendMailRequest;
import site.code4fun.repository.SearchSpecification;
import site.code4fun.repository.jpa.ContactRepository;
import site.code4fun.scheduler.Scheduler;
import site.code4fun.service.email.MailChimp;
import site.code4fun.service.queue.QueueService;
import site.code4fun.util.UrlParserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static site.code4fun.constant.AppConstants.SEARCH_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
@Lazy
public class ContactService extends AbstractBaseService<ContactEntity, Long> {
    @Getter(AccessLevel.PROTECTED)
    private final ContactRepository repository;

    @Qualifier("redisImpl")
    private final QueueService queueService;
    private final Scheduler scheduler;
    private final MailChimp mailChimp;

    @Override
    public Page<ContactEntity> getPaging(Map<String, String> mapRequestParam) {
        Pageable pageReq = buildPageRequest(mapRequestParam);

        List<SearchCriteria> lstSearch = UrlParserUtils.parserQueryString(mapRequestParam, ContactEntity.class);
        Specification<ContactEntity> spec = null;

        String queryString = mapRequestParam.get(SEARCH_KEY);
        if (isNotBlank(queryString)) {
            spec = new SearchSpecification<>(new SearchCriteria("name", SearchOperator.EQUAL, queryString));
            spec = spec.or(new SearchSpecification<>(new SearchCriteria("email", SearchOperator.EQUAL, queryString)));
            spec = spec.or(new SearchSpecification<>(new SearchCriteria("phone", SearchOperator.EQUAL, queryString)));
        }

        if (!lstSearch.isEmpty()) {
            if (spec == null) {
                spec = new SearchSpecification<>(lstSearch.get(0));
            }

            for (SearchCriteria searchCriteria : lstSearch) {
                spec = spec.and(new SearchSpecification<>(searchCriteria));
            }
        }

        return null != spec ? getRepository().findAll(spec, pageReq) : getRepository().findAll(pageReq);
    }

    @SneakyThrows
    public void bulkSend(SendMailRequest request) {
        log.info("Creating new bulk send request {}", request.toString());
        List<ContactEntity> lstEntity = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(request.getContacts())){
            lstEntity = getRepository().findAllById(request.getContacts());

        }
        List<String> recipients = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(request.getRecipients())){
            recipients.addAll(request.getRecipients().stream().map(SendMailRequest.Recipient::getEmail).toList());
        }

        if ("sms".equalsIgnoreCase(request.getType())){
            recipients.addAll(lstEntity.stream().map(ContactEntity::getPhone).toList());
        } else {
            recipients.addAll(lstEntity.stream().map(ContactEntity::getEmail).toList());
        }

        if (request.getSendTime() == SendMailRequest.SendTime.INSTANT){
            addToQueue(request.getType(), request.getSubject(), request.getContent(), recipients);
        } else {
            scheduler.scheduleSend(
                    recipients,
                    request.getType(),
                    request.getSubject(),
                    request.getContent(),
                    request.getScheduleExp()
            );
        }
    }

    public void addToQueue(String type, String subject, String content, List<String> recipients) {
        if ("sms".equalsIgnoreCase(type)) {
            addSmsToQueue(recipients, subject, content);
        } else {
            addMailsToQueue(recipients, subject, content);
        }
    }

    private void addMailsToQueue(List<String> recipients, String subject, String content) {
        recipients.stream().filter(StringUtils::isNotBlank).forEach(email -> {
            SendMailDTO dto = new SendMailDTO();
            dto.setSubject(subject);
            dto.setContent(content);
            dto.setEmailAddress(email);
            queueService.sendSmsOrMailContact(QueueName.KAFKA_TOPIC_NAME_SEND_MAIL, dto);
        });
    }

    private void addSmsToQueue(List<String> recipients, String subject, String content) {
        recipients.stream().filter(StringUtils::isNotBlank).forEach(phone -> {
            SendMailDTO dto = new SendMailDTO();
            dto.setSubject(subject);
            dto.setContent(content);
            dto.setPhone(phone);
            queueService.sendSmsOrMailContact(QueueName.KAFKA_TOPIC_NAME_SEND_SMS, dto);
        });
    }

    public List<ContactEntity> sync() {
        log.info("Start Sync contact....");
        List<ContactEntity> lst = new ArrayList<>();
        mailChimp.getAllAudience().forEach(audience ->
            mailChimp.getAllAudienceMember(audience.getId()).forEach(member -> {
            ContactEntity entity = new ContactEntity();
            entity.setProviderId(member.getId());
            entity.setEmail(member.getEmail_address());
            entity.setPhone(member.getSms_phone_number());
            entity.setName(member.getFull_name());
            entity.setSource(member.getSource());
            if (member.getMerge_fields() != null && member.getMerge_fields().getADDRESS() != null){
                Address address = new Address();
                address.setAddress1(member.getMerge_fields().getADDRESS().getAddr1());
                address.setAddress2(member.getMerge_fields().getADDRESS().getAddr2());
                address.setCity(member.getMerge_fields().getADDRESS().getCity());
                address.setCountry(member.getMerge_fields().getADDRESS().getCountry());
                address.setZip(member.getMerge_fields().getADDRESS().getZip());
                entity.setAddress(address);
            }

            lst.add(entity);
        }));
        log.info("Done Sync contact....");
        return getRepository().saveAll(lst);
    }

}