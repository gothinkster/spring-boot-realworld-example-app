package io.spring.application.comment;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.comment.MyBatisCommentRepository;
import io.spring.infrastructure.user.MyBatisUserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@MybatisTest
@RunWith(SpringRunner.class)
@Import({MyBatisCommentRepository.class, MyBatisUserRepository.class, CommentQueryService.class})
public class CommentQueryServiceTest {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentQueryService commentQueryService;
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
}