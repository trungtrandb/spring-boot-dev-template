package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Maintenance implements Serializable {
    private String title;
    private String buttonTitleOne;
    private String newsLetterTitle;
    private String buttonTitleTwo;
    private String contactUsTitle;
    private String aboutUsTitle;
    private boolean isOverlayColor;
    private Object overlayColor;
    private Object overlayColorRange;
    private String description;
    private String newsLetterDescription;
    private String aboutUsDescription;
    private AttachmentDTO image;
    private Date start;
    private Date until;
}