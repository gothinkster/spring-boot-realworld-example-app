package io.spring.application.comment;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface CommentReadService {
    CommentData findById(String id);
}
