package site.code4fun.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class Social implements Serializable {
    private String url;
    private Icon icon;

    public enum Icon{
        FacebookIcon,
        TwitterIcon,
        InstagramIcon
    }
}