package site.code4fun;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.client.RestTemplate;
import site.code4fun.service.google.GoogleOauth2Properties;
import site.code4fun.service.google.GoogleServiceAccountProperties;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static site.code4fun.constant.AppConstants.TIME_ZONE;

@SpringBootApplication
@EnableWebSecurity
@EnableMethodSecurity
@EnableCaching
@EnableScheduling
@EnableAsync
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@EnableConfigurationProperties({GoogleOauth2Properties.class, GoogleServiceAccountProperties.class})
//@EnableElasticsearchRepositories
@Slf4j
public class Application {
	private static final List<String> requiredEnvs = Arrays.asList(
			"mailchimp.server-prefix",
			"spring.mail.username",
			"spring.mail.password"
	);
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	@Bean
	public static BeanFactoryPostProcessor beanFactoryPostProcessor(Environment environment) { // https://i.sstatic.net/jg555.png
        requiredEnvs.forEach(envParam -> {
            String envVal = environment.getProperty(envParam, String.class);
            if (isEmpty(envVal)) {
                log.error("{} param is missing from config, please add in application.properties", envParam);
            }
        });

        // Default system time zone
        return beanFactory -> TimeZone.setDefault(TimeZone.getTimeZone(TIME_ZONE));
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
