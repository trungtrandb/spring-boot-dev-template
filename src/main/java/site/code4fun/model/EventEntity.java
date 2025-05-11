package site.code4fun.model;

import jakarta.persistence.*;
import lombok.Data;
import site.code4fun.constant.AppConstants;
import site.code4fun.constant.Status;

import java.util.Date;

@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "event")
public class EventEntity extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    private Date start;
    private Date end;
    private String eventColor;
    private Status status;
}