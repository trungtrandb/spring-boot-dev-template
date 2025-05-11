package site.code4fun.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.JDBCException;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.code4fun.model.dto.ResponseDTO;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
@Component
public class ApiExceptionHandler implements AccessDeniedHandler {

    @Value("${aws.cloud-watch.group-name}")
    private String cloudWatchGroupName;
    @Value("${aws.cloud-watch.stream-name}")
    private String cloudWatchStreamName;
    private final Environment environment;
    private final CloudWatchLogsClient cloudWatchLogsClient;
    private final Gson gson;
    private DescribeLogStreamsResponse describeLogStreamsResponse;

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseDTO handleNotFoundException(NotFoundException ex) {
        sendLogToCloudWatch(ex);
        return new ResponseDTO(ResponseDTO.Type.danger, "Not found", 404, ex.getMessage(), null);
    }

    @ExceptionHandler({ValidationException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseDTO handleValidationException(ValidationException ex) {
        sendLogToCloudWatch(ex);
        return new ResponseDTO(ResponseDTO.Type.danger, "Validation", 404, ex.getMessage(), null);
    }


    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ResponseDTO handleDuplicateException(DuplicateResourceException ex) {
        sendLogToCloudWatch(ex);
        return new ResponseDTO(ResponseDTO.Type.danger, "Duplicate", 401, ex.getMessage(), null);
    }

    @ExceptionHandler({JDBCException.class, JDBCConnectionException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseDTO handleJDBCException(Exception ex) {
        sendLogToCloudWatch(ex);
        return new ResponseDTO(ResponseDTO.Type.danger, "Error", null, ex.getClass().getName(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @SneakyThrows
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex){
        if (!response.isCommitted()) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            var value = new ResponseDTO(ResponseDTO.Type.danger, "Error", null, ex.getMessage(), null);

            new ObjectMapper().writeValueAsString(value);
        }
    }

    /**
     * Tất cả các Exception không được khai báo sẽ được xử lý tại đây
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseDTO handleAllException(Exception ex) {
        sendLogToCloudWatch(ex);
        return new ResponseDTO(ResponseDTO.Type.danger, "Error", null, ex.getMessage(), null);
    }

    private void sendLogToCloudWatch(Exception exception) {
        boolean isLocal = Arrays.stream(environment.getActiveProfiles()).anyMatch(x -> StringUtils.equals(x, "local"));
        if (!isLocal) {
            CompletableFuture.runAsync(() -> {
                String sequenceToken = getStreamSequenceToken();
                if (isNotBlank(sequenceToken)) {
                    InputLogEvent inputLogEvent = InputLogEvent.builder()
                            .message(buildLogMessage(exception))
                            .timestamp(System.currentTimeMillis())
                            .build();

                    PutLogEventsRequest putLogEventsRequest = PutLogEventsRequest.builder()
                            .logEvents(Collections.singletonList(inputLogEvent))
                            .logGroupName(cloudWatchGroupName)
                            .logStreamName(cloudWatchStreamName)
                            .sequenceToken(sequenceToken)
                            .build();

                    cloudWatchLogsClient.putLogEvents(putLogEventsRequest);
                }
            });
        } else {
            exception.printStackTrace(); //NOSONAR
        }
    }

    private String buildLogMessage(Exception exception) {
        final Map<String, String> message = new HashMap<>();
        message.put("class", exception.getClass().toString());
        message.put("message", exception.getMessage());
        message.put("cause", Arrays.toString(exception.getStackTrace()));
        return gson.toJson(message);
    }

    private String getStreamSequenceToken() {
        String sequenceToken = "";
        if (describeLogStreamsResponse == null) {
            DescribeLogStreamsRequest logStreamRequest = DescribeLogStreamsRequest.builder()
                    .logGroupName(cloudWatchGroupName)
                    .logStreamNamePrefix(cloudWatchStreamName)
                    .build();
            describeLogStreamsResponse = cloudWatchLogsClient.describeLogStreams(logStreamRequest);
        }

        if (describeLogStreamsResponse != null) {
            sequenceToken = describeLogStreamsResponse.logStreams().get(0).uploadSequenceToken();
        }
        return sequenceToken;
    }
}
