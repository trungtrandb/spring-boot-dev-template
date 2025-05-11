package site.code4fun.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import site.code4fun.constant.DayOfMonth;
import site.code4fun.constant.Status;
import site.code4fun.model.AttendanceEntity;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;

@Data
public class ShiftDTO implements Serializable {
    private Long id;
    private String name;
    private Status status;
    @JsonFormat(pattern="kk:mm")
    private LocalTime startTime;
    @JsonFormat(pattern="kk:mm")
    private LocalTime endTime;
    private List<DayOfWeekRec> weekOff;
    private List<DayOfMonth> workingDays;
    @JsonFormat(pattern="kk:mm")
    private LocalTime breakStartTime;
    @JsonFormat(pattern="kk:mm")
    private LocalTime breakEndTime;
    private AttendanceEntity.Status defaultStatusWhenNoCheckin;

    public record DayOfWeekRec(String label, String value){}
}
