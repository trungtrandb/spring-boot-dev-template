package site.code4fun.service.email;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("unused")
@Slf4j
public class MailChimp {
    @Value("${mailchimp.api-key}")
    private String apiKey;
    @Value("${mailchimp.server-prefix}")
    private String serverPrefix;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ROOT_ENDPOINT = "https://%s.api.mailchimp.com/3.0";


    public void createMailTemplate(){
        Map<String, String> template = new HashMap<>();
        template.put("name", "Test Template");
        template.put("html", "<h1>This is html part</h1>");
        HttpEntity<Template> res = getTemplate().exchange("/templates", HttpMethod.POST, buildHttpEntity(template), Template.class);
    }

    public void createCampaign(){
        var templates = getAllMailTemplate();
        Map<String, Object> recipients =  new HashMap<>();
        recipients.put("list_id", "7915970ac7");

        Map<String, Object> settings =  new HashMap<>();
        settings.put("title", "CampaignTitle");
        settings.put("template_id", templates.get(0).id());

        Map<String, Object> template = new HashMap<>();
        template.put("type", "plaintext"); // regular", "plaintext", "absplit", "rss", or "variate".
        template.put("recipients", recipients);
        template.put("settings" ,settings);
        HttpEntity<Campaign> res = getTemplate().exchange("/campaigns", HttpMethod.POST, buildHttpEntity(template), Campaign.class);
        log.info(res.toString());
    }

    public Audience createAudience(){ // Free tier limit 1 audience
        Contact contact = new Contact();
        contact.setCompany("Company Name");
        contact.setAddress1("address 1");
        contact.setCity("HCM");
        contact.setCountry("VN");

        CampaignDefault campaignDefault = new CampaignDefault();
        campaignDefault.setLanguage("en");
        campaignDefault.setSubject("Test subject");
        campaignDefault.setFromName("Test");
        campaignDefault.setFromEmail("test@gmail.com");

        Audience audience = new Audience();
        audience.setName("Test Audience");
        audience.setContact(contact);
        audience.setEmail_type_option(true);
        audience.setCampaign_defaults(campaignDefault);
        audience.setPermission_reminder("You are receiving this email because you opted in.");

        HttpEntity<Audience> res = getTemplate().exchange("/lists", HttpMethod.POST, buildHttpEntity(audience), Audience.class);
        return res.getBody();
    }

    @SneakyThrows
    public List<Template> getAllMailTemplate(){
        HttpEntity<JsonNode> res = getTemplate().exchange("/templates", HttpMethod.GET, buildHttpEntity(), JsonNode.class);
        var objectReader =  objectMapper.readerFor(new TypeReference<List<Template>>() {});
        var body = res.getBody();
        return body != null ? objectReader.readValue(body.get("templates")) : Collections.emptyList();
    }

    @SneakyThrows
    public List<Campaign> getAllCampaign(){
        HttpEntity<JsonNode> res = getTemplate().exchange("/campaigns", HttpMethod.GET, buildHttpEntity(), JsonNode.class);
        var objectReader =  objectMapper.readerFor(new TypeReference<List<Campaign>>() {});
        var body = res.getBody();
        return body != null ? objectReader.readValue(body.get("campaigns")) : Collections.emptyList();
    }

    @SneakyThrows
    public List<Audience> getAllAudience(){
        HttpEntity<JsonNode> res = getTemplate().exchange("/lists", HttpMethod.GET, buildHttpEntity(), JsonNode.class);
        var objectReader =  objectMapper.readerFor(new TypeReference<List<Audience>>() {});
        var body = res.getBody();
        return body != null ? objectReader.readValue(body.get("lists")) : Collections.emptyList();
    }

    @SneakyThrows
    public List<Member> getAllAudienceMember(String audienceId){
        HttpEntity<JsonNode> res = getTemplate().exchange("/lists/" + audienceId + "/members", HttpMethod.GET, buildHttpEntity(), JsonNode.class);
        var objectReader =  objectMapper.readerFor(new TypeReference<List<Member>>() {});
        var body = res.getBody();
        return body != null ? objectReader.readValue(body.get("members")) : Collections.emptyList();
    }

    public Member createMemberInAudience(String audienceId){
        Member member = new Member();
        member.setEmail_address("trungtrandb5@gmail.com");
        member.setStatus("subscribed");
        HttpEntity<Member> res = getTemplate().exchange("/lists/" + audienceId + "/members", HttpMethod.POST, buildHttpEntity(member), Member.class);
        return res.getBody();
    }

    private HttpEntity<?> buildHttpEntity(Object body){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("key", apiKey);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<?> buildHttpEntity(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("key", apiKey);
        return new HttpEntity<>(null, headers);
    }

    private String buildUrl(){
        return String.format(ROOT_ENDPOINT, serverPrefix);
    }
    private RestTemplate getTemplate(){
        return new RestTemplateBuilder()
                .rootUri(buildUrl())
                .build();
    }
}
