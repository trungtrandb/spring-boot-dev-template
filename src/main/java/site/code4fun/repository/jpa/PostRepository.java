package site.code4fun.repository.jpa;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import site.code4fun.constant.PostType;
import site.code4fun.constant.Status;
import site.code4fun.model.Post;

public interface PostRepository extends BaseRepository<Post, Long> {
    @Query(value = "SELECT p FROM Post p JOIN p.postContents pc where pc.slug = ?1 and p.status = 'ACTIVE'")
    Post findBydPostContents_slug(String slug);

    Page<Post> findAllByStatusEqualsAndTypeEquals(Status status, PostType type, Pageable pageable);

    List<Post> findAllByStatusEqualsAndTypeEquals(Status status, PostType type);
}

