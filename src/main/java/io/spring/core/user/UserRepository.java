package io.spring.core.user;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {
  void save(User user);

  Optional<User> findById(String id);

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  void saveRelation(FollowRelation followRelation);

  Optional<FollowRelation> findRelation(String userId, String targetId);

  void removeRelation(FollowRelation followRelation);
}
