package io.spring.core.article;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Data
@EqualsAndHashCode(of = "name")
public class Tag {
    private String id;
    private String name;

    public Tag(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }
}
