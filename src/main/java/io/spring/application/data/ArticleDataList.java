package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@Getter
public class ArticleDataList {
  @JsonProperty("articles")
  private final List<ArticleData> articleDatas;

  @JsonProperty("articlesCount")
  private final int count;

  public ArticleDataList(List<ArticleData> articleDatas, int count) {

    this.articleDatas = articleDatas;
    this.count = count;
  }
}
