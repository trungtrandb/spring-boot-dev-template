package site.code4fun.model.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Email
@Data
public class ProductSize {
    private int length;
    private int width;
    private int height;
    private int weight;
}
