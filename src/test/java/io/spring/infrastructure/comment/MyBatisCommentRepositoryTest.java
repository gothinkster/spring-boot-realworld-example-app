package io.spring.infrastructure.comment;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.infrastructure.repository.MyBatisCommentRepository;
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
@Import({MyBatisCommentRepository.class})
public class MyBatisCommentRepositoryTest {
    @Autowired
    private CommentRepository commentRepository;

    @Test
    public void should_create_and_fetch_comment_success() throws Exception {
        Comment comment = new Comment("content", "123", "456");
        commentRepository.save(comment);

        Optional<Comment> optional = commentRepository.findById("456", comment.getId());
        assertThat(optional.isPresent(), is(true));
        assertThat(optional.get(), is(comment));
    }
}