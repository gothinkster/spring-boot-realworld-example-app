package io.spring.application.article;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@JsonRootName("article")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewArticleParam {

    @NotBlank(message = "can't be empty")
    @DuplicatedArticleConstraint
    private String title;

    @NotBlank(message = "can't be empty")
    private String description;

    @NotBlank(message = "can't be empty")
    private String body;

    private List<String> tagList;

}
