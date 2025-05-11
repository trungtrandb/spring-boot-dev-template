package site.code4fun.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.code4fun.service.AttendanceService;
import site.code4fun.service.integrations.VietCapService;

import java.util.Date;
import java.util.List;

import static site.code4fun.constant.AppConstants.*;
import static site.code4fun.util.RandomUtils.random;

@Component
@RequiredArgsConstructor
@Slf4j
public class Scheduler {
    private final CacheManager cacheManager;
    private final org.quartz.Scheduler quartzScheduler;
    private final AttendanceService attendanceService;
    private final VietCapService vciRest;

    @SneakyThrows
    public void scheduleSend(List<String> recipients, String type, String subject, String body, Date sendTime) {
        if (sendTime.before(new Date())) {
            log.warn("will not send because sendTime {} is after current time", sendTime);
            return;
        }

        JobDetail jobDetail = buildJobDetail(recipients, type, subject, body);
        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "email-triggers")
                .withDescription("Send Email Trigger")
                .startAt(sendTime)
                .build();
        quartzScheduler.scheduleJob(jobDetail, trigger);
        log.info("Added job to quartz Scheduler with jobName {}", jobDetail.getKey().getName());
    }

    @Scheduled(cron = "0 */5 * * * *")
    private void evictAllCache() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) cache.clear();
        });
    }

    @Scheduled(cron = "0 10 0 * * *")
    private void createAttendance() {
        attendanceService.createAttendanceEveryDay();
    }

    @Scheduled(cron = "0 0 0 * * *")
    private void pullUpdateStockSymbols() {
        log.info("pull update stock symbols");
        vciRest.pullUpdateAllSymbols();
        log.info("pull update stock symbols done");
    }

    private JobDetail buildJobDetail(List<String> recipients, String type, String subject, String body) {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put(JOB_PARAM_RECIPIENTS, recipients.toString());
        jobDataMap.put(JOB_PARAM_SUBJECT, subject);
        jobDataMap.put(JOB_PARAM_BODY, body);
        jobDataMap.put(JOB_PARAM_TYPE, type);

        return JobBuilder.newJob(EmailJob.class)
                .withIdentity(random(4), "email-jobs")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }
}