package com.giveitup.giveitup_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class RoleEntity {
    @Id
    String name;

    String description;

    @ManyToMany
    Set<Permission> permissions;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private Set<UserEntity> users;
}
