package io.spring.infrastructure.mybatis.readservice;

import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRelationshipQueryService {
  boolean isUserFollowing(
      @Param("userId") String userId, @Param("anotherUserId") String anotherUserId);

  Set<String> followingAuthors(@Param("userId") String userId, @Param("ids") List<String> ids);

  List<String> followedUsers(@Param("userId") String userId);
}
