package io.spring.application.comment;

import io.spring.application.CommentQueryService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisCommentRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Import({
        MyBatisCommentRepository.class,
        MyBatisUserRepository.class,
        CommentQueryService.class,
        MyBatisArticleRepository.class
})
public class CommentQueryServiceTest extends DbTestBase {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentQueryService commentQueryService;

    @Autowired
    private ArticleRepository articleRepository;

    private User user;


    @Before
    public void setUp() {
        user = new User("aisensiy@test.com", "aisensiy", "123", "", "");
        userRepository.save(user);
    }


    @Test
    public void should_read_comment_success() {
        var comment = new Comment("content", user.getId(), "123");
        commentRepository.save(comment);

        var optional = commentQueryService.findById(comment.getId(), user);
        assertTrue(optional.isPresent());

        var commentData = optional.get();
        assertEquals(commentData.getProfileData().getUsername(), user.getUsername());
    }


    @Test
    public void should_read_comments_of_article() {
        var article = new Article("title", "desc", "body", List.of("java"), user.getId());
        articleRepository.save(article);

        var user2 = new User("user2@email.com", "user2", "123", "", "");
        userRepository.save(user2);
        userRepository.saveRelation(new FollowRelation(user.getId(), user2.getId()));

        var comment1 = new Comment("content1", user.getId(), article.getId());
        commentRepository.save(comment1);

        var comment2 = new Comment("content2", user2.getId(), article.getId());
        commentRepository.save(comment2);

        var comments = commentQueryService.findByArticleId(article.getId(), user);
        assertEquals(comments.size(), 2);
    }

}
