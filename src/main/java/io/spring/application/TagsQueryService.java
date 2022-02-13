package io.spring.application;

import io.spring.infrastructure.mybatis.readservice.TagReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagsQueryService {

    private final TagReadService tagReadService;


    public List<String> allTags() {
        return tagReadService.all();
    }

}
