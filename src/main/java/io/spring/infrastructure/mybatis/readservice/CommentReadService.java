package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.CursorPageParameter;
import io.spring.application.data.CommentData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.joda.time.DateTime;

@Mapper
public interface CommentReadService {
  CommentData findById(@Param("id") String id);

  List<CommentData> findByArticleId(@Param("articleId") String articleId);

  List<CommentData> findByArticleIdWithCursor(
      @Param("articleId") String articleId, @Param("page") CursorPageParameter<DateTime> page);
}
