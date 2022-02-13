package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import graphql.relay.DefaultConnectionCursor;
import graphql.relay.DefaultPageInfo;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.graphql.DgsConstants.ARTICLE;
import io.spring.graphql.DgsConstants.COMMENTPAYLOAD;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentEdge;
import io.spring.graphql.types.CommentsConnection;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@DgsComponent
@RequiredArgsConstructor
public class CommentDatafetcher {

    private final CommentQueryService commentQueryService;


    @DgsData(parentType = COMMENTPAYLOAD.TYPE_NAME, field = COMMENTPAYLOAD.Comment)
    public DataFetcherResult<Comment> getComment(DgsDataFetchingEnvironment dfe) {
        var comment = dfe.<CommentData>getLocalContext();
        var commentResult = buildCommentResult(comment);
        return DataFetcherResult.<Comment>newResult()
                .data(commentResult)
                .localContext(Map.of(comment.getId(), comment))
                .build();
    }


    @DgsData(parentType = ARTICLE.TYPE_NAME, field = ARTICLE.Comments)
    public DataFetcherResult<CommentsConnection> articleComments(
            @InputArgument("first") Integer first,
            @InputArgument("after") String after,
            @InputArgument("last") Integer last,
            @InputArgument("before") String before,
            DgsDataFetchingEnvironment dfe
    ) {
        if (first == null && last == null)
            throw new IllegalArgumentException("first and last are null");

        var current = SecurityUtils.getCurrentUser().orElse(null);
        var article = dfe.<Article>getSource();
        var map = dfe.<Map<String, ArticleData>>getLocalContext();
        var articleData = map.get(article.getSlug());
        var dateTimeCursorPageParameter = first == null ?
                new CursorPageParameter<>(DateTimeCursor.parse(before), last, Direction.PREV) :
                new CursorPageParameter<>(DateTimeCursor.parse(after), first, Direction.NEXT);
        var comments = commentQueryService.findByArticleIdWithCursor(
                articleData.getId(),
                current,
                dateTimeCursorPageParameter
        );

        var pageInfo = buildCommentPageInfo(comments);
        var result = CommentsConnection.newBuilder()
                .pageInfo(pageInfo)
                .edges(comments.getData()
                        .stream()
                        .map(getCommentDataCommentEdgeFunction())
                        .toList()
                )
                .build();
        return DataFetcherResult.<CommentsConnection>newResult()
                .data(result)
                .localContext(comments.getData()
                        .stream()
                        .collect(toMap(CommentData::getId, c -> c))
                )
                .build();
    }


    @NotNull
    private Function<CommentData, CommentEdge> getCommentDataCommentEdgeFunction() {
        return commentData -> CommentEdge.newBuilder()
                .cursor(commentData.getCursor().toString())
                .node(buildCommentResult(commentData))
                .build();
    }


    private DefaultPageInfo buildCommentPageInfo(CursorPager<CommentData> comments) {
        var startCursor = comments.getStartCursor();
        var endCursor = comments.getEndCursor();
        return new DefaultPageInfo(
                startCursor == null ? null : new DefaultConnectionCursor(startCursor.toString()),
                endCursor == null ? null : new DefaultConnectionCursor(endCursor.toString()),
                comments.hasPrevious(),
                comments.hasNext()
        );
    }


    private Comment buildCommentResult(CommentData comment) {
        String print = ISODateTimeFormat.dateTime().withZoneUTC().print(comment.getCreatedAt());
        return Comment.newBuilder()
                .id(comment.getId())
                .body(comment.getBody())
                .updatedAt(print)
                .createdAt(print)
                .build();
    }

}
