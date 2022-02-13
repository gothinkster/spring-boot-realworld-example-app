package io.spring.infrastructure.repository;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.infrastructure.mybatis.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MyBatisCommentRepository implements CommentRepository {

    private final CommentMapper commentMapper;


    @Override
    public void save(Comment comment) {
        commentMapper.insert(comment);
    }

    @Override
    public Optional<Comment> findById(String articleId, String id) {
        return Optional.ofNullable(commentMapper.findById(articleId, id));
    }

    @Override
    public void remove(Comment comment) {
        commentMapper.delete(comment.getId());
    }

}
