package io.spring.core.comment;

import java.util.Optional;

public interface  CommentRepository {
    void save(Comment comment);

    Optional<Comment> findById(String id);
}
