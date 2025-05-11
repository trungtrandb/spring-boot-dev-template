package site.code4fun.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import site.code4fun.service.ContactService;

import java.util.Arrays;
import java.util.List;

import static site.code4fun.constant.AppConstants.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailJob extends QuartzJobBean {
    private final ContactService contactService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        log.info("Executing Job with key {}", jobExecutionContext.getJobDetail().getKey());

        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        String subject = jobDataMap.getString(JOB_PARAM_SUBJECT);
        String body = jobDataMap.getString(JOB_PARAM_BODY);
        List<String> recipients = Arrays.stream(jobDataMap.getString(JOB_PARAM_RECIPIENTS).split(COMMA)).toList();
        String type = jobDataMap.getString(JOB_PARAM_TYPE);
        contactService.addToQueue(type, subject, body, recipients);
    }
}