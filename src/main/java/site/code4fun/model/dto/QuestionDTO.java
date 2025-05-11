package site.code4fun.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class QuestionDTO{
    private Long id;
    private String answer;
    private Date created;
    private Long productId;
//    private String my_feedback,
//    negative_feedbacks_count,
//    positive_feedbacks_count,
    private String question;
}