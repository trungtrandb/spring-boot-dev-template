package site.code4fun.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import site.code4fun.ApplicationProperties;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.Locale;

import static site.code4fun.constant.AppConstants.LOCALE_RESOLVE_NAME;

@Configuration
@RequiredArgsConstructor
@Slf4j
class WebConfig implements WebMvcConfigurer {

	private final ApplicationProperties properties;


    @Bean
    public OpenAPI openApiConfig() {
        return new OpenAPI()
                .info(new Info()
                        .title(properties.getAppName())
                        .version("v" + properties.getAppVersion())
                        .description("Spring boot application for multi purpose")
                        .contact(new Contact().email("trungtrandb@gmail.com").name("TrungTQ"))
                );
    }

	@Bean
	public LocaleResolver localeResolver() {
		CookieLocaleResolver resolver = new CookieLocaleResolver(LOCALE_RESOLVE_NAME);
		resolver.setDefaultLocale(Locale.ENGLISH);
		resolver.setCookieMaxAge(Duration.ofSeconds(60 * 60 * 24L)); // 24h
		return resolver;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
	}

	private LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
		interceptor.setParamName(LOCALE_RESOLVE_NAME); // The parameter name for language change (e.g., ?language=en)
		return interceptor;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**")
				.addResourceLocations("classpath:/public/");
	}

	@Bean
	public Gson gson() {
		DecimalFormat df = new DecimalFormat("#,###,###.00", new DecimalFormatSymbols(Locale.getDefault()));
		return new GsonBuilder()
				.serializeSpecialFloatingPointValues()
				.registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, type, jsonSerializationContext) -> new JsonPrimitive(df.format(src)))
				.create();
	}
}
