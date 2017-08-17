package io.spring.application.tag;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface TagReadService {
    List<String> all();
}
