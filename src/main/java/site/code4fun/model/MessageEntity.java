package site.code4fun.model;


import jakarta.persistence.*;
import lombok.Data;
import site.code4fun.constant.AppConstants;


@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "message")
public class MessageEntity extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoomEntity room;

}