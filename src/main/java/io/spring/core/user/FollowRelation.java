package io.spring.core.user;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FollowRelation {
    private String userId;
    private String targetId;

    public FollowRelation(String userId, String targetId) {

        this.userId = userId;
        this.targetId = targetId;
    }
}
