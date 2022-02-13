package io.spring.api;

import io.spring.application.TagsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(path = "tags")
@RequiredArgsConstructor
public class TagsApi {

    private final TagsQueryService tagsQueryService;


    @GetMapping
    public ResponseEntity<?> getTags() {
        return ResponseEntity.ok(Map.of("tags", tagsQueryService.allTags()));
    }

}
