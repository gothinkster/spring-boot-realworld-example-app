package io.spring.infrastructure.comment;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisCommentRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Import({MyBatisCommentRepository.class})
public class MyBatisCommentRepositoryTest extends DbTestBase {

    @Autowired
    private CommentRepository commentRepository;

    @Test
    public void should_create_and_fetch_comment_success() {
        var comment = new Comment("content", "123", "456");
        commentRepository.save(comment);

        var optional = commentRepository.findById("456", comment.getId());
        assertTrue(optional.isPresent());
        assertEquals(optional.get(), comment);
    }

}
