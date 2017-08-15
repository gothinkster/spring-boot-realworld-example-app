package io.spring.infrastructure.comment;

import io.spring.core.comment.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface CommentMapper {
    void insert(@Param("comment") Comment comment);

    Comment findById(@Param("id") String id);
}
