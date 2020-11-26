package io.spring.core.article;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArticleTest {

    @Test
    public void should_get_right_slug() {
        Article article = new Article("a new   title", "desc", "body", new String[]{"java"}, "123");
        assertThat(article.getSlug(), is("a-new-title"));
    }

    @Test
    public void should_get_right_slug_with_number_in_title() {
        Article article = new Article("a new title 2", "desc", "body", new String[]{"java"}, "123");
        assertThat(article.getSlug(), is("a-new-title-2"));
    }

    @Test
    public void should_get_lower_case_slug() {
        Article article = new Article("A NEW TITLE", "desc", "body", new String[]{"java"}, "123");
        assertThat(article.getSlug(), is("a-new-title"));
    }

    @Test
    public void should_handle_other_language() {
        Article article = new Article("中文：标题", "desc", "body", new String[]{"java"}, "123");
        assertThat(article.getSlug(), is("中文-标题"));
    }

    @Test
    public void should_handle_commas() {
        Article article = new Article("what?the.hell,w", "desc", "body", new String[]{"java"}, "123");
        assertThat(article.getSlug(), is("what-the-hell-w"));
    }
}