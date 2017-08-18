package io.spring.application.comment;

import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisCommentRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@MybatisTest
@RunWith(SpringRunner.class)
@Import({MyBatisCommentRepository.class, MyBatisUserRepository.class, CommentQueryService.class, MyBatisArticleRepository.class})
public class CommentQueryServiceTest {
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
    public void setUp() throws Exception {
        user = new User("aisensiy@test.com", "aisensiy", "123", "", "");
        userRepository.save(user);
    }

    @Test
    public void should_read_comment_success() throws Exception {
        Comment comment = new Comment("content", user.getId(), "123");
        commentRepository.save(comment);

        Optional<CommentData> optional = commentQueryService.findById(comment.getId(), user);
        assertThat(optional.isPresent(), is(true));
        CommentData commentData = optional.get();
        assertThat(commentData.getProfileData().getUsername(), is(user.getUsername()));
    }

    @Test
    public void should_read_comments_of_article() throws Exception {
        Article article = new Article("title", "desc", "body", new String[]{"java"}, user.getId());
        articleRepository.save(article);

        User user2 = new User("user2@email.com", "user2", "123", "", "");
        userRepository.save(user2);
        userRepository.saveRelation(new FollowRelation(user.getId(), user2.getId()));

        Comment comment1 = new Comment("content1", user.getId(), article.getId());
        commentRepository.save(comment1);
        Comment comment2 = new Comment("content2", user2.getId(), article.getId());
        commentRepository.save(comment2);

        List<CommentData> comments = commentQueryService.findByArticleId(article.getId(), user);
        assertThat(comments.size(), is(2));

    }
}