package site.code4fun.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "ai_prompt")
public class PromptEntity extends Auditable{

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(length = 5000)
    private String content;
}