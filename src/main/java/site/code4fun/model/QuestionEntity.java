package site.code4fun.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import site.code4fun.constant.AppConstants;

@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "question")
public class QuestionEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Câu hỏi không được để trống")
    @Column(nullable = false, length = 1000)
    private String question;

    @NotBlank(message = "Câu trả lời không được để trống")
    @Column(nullable = false, length = 2000)
    private String answer;

    @NotNull(message = "ID sản phẩm không được để trống")
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
}