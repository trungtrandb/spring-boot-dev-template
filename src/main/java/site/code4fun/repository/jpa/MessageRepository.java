package site.code4fun.repository.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import site.code4fun.model.MessageEntity;

public interface MessageRepository extends BaseRepository<MessageEntity, Long> {
    @EntityGraph(attributePaths = {"room"})
    Page<MessageEntity> findByRoomId(Long roomId, Pageable pageable);

    MessageEntity findFirstByRoom_idOrderByCreatedDesc(Long roomId);
}