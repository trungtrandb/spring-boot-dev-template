package site.code4fun.model.dto;

import java.io.Serializable;
import java.util.List;

public record ReviewDTO(String comment, Long orderId, List<?> photos, Long productId, Integer rating,
                        UserLite user) implements Serializable {

}


