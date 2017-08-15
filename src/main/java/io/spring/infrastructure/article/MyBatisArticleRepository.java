package io.spring.infrastructure.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisArticleRepository implements ArticleRepository {
    private ArticleMapper articleMapper;

    public MyBatisArticleRepository(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    @Override
    public void save(Article article) {
        if (articleMapper.findById(article.getId()) == null) {
            createNew(article);
        } else {
            articleMapper.update(article);
        }
    }

    private void createNew(Article article) {
        articleMapper.insert(article);
        for (Tag tag : article.getTags()) {
            if (!articleMapper.findTag(tag.getName())) {
                articleMapper.insertTag(tag);
            }
            articleMapper.insertArticleTagRelation(article.getId(), tag.getId());
        }
    }

    @Override
    public Optional<Article> findById(String id) {
        return Optional.ofNullable(articleMapper.findById(id));
    }

    @Override
    public Optional<Article> findBySlug(String slug) {
        return Optional.ofNullable(articleMapper.findBySlug(slug));
    }
}
