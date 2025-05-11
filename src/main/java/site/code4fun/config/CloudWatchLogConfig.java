package site.code4fun.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import site.code4fun.ApplicationProperties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

@Configuration
@RequiredArgsConstructor
public class  CloudWatchLogConfig {

    private final ApplicationProperties properties;

    @Bean
    public CloudWatchLogsClient cloudWatchLogsClient() {
        AwsCredentialsProvider credentialsProvider =  StaticCredentialsProvider.create(AwsBasicCredentials.create(properties.getAccessKeyId(), properties.getSecretAccessKey()));

        return CloudWatchLogsClient
                .builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
