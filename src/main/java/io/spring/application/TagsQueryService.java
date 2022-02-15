package io.spring.application;

import io.spring.infrastructure.mybatis.readservice.TagReadService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TagsQueryService {
  private TagReadService tagReadService;

  public TagsQueryService(TagReadService tagReadService) {
    this.tagReadService = tagReadService;
  }

  public List<String> allTags() {
    return tagReadService.all();
  }
}
