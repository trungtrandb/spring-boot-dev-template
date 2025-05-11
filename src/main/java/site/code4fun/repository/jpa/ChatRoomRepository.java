package site.code4fun.repository.jpa;

import site.code4fun.model.ChatRoomEntity;

import java.util.List;

public interface ChatRoomRepository extends BaseRepository<ChatRoomEntity, Long> {
    List<ChatRoomEntity> findByUsers_id(long userId);

    List<ChatRoomEntity> findByUsers_idAndIdIn(long userId, List<Long> ids);
}

