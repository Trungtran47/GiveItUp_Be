package com.giveitup.giveitup_be.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "username", unique = true, columnDefinition = "NVARCHAR(255)")
    String username;
    String password;
    String firstName;
    String lastName;
    LocalDate dob;
    String email;
    String phoneNumber;
    Long gender;

//    @ManyToMany
//    Set<RoleEntity> roleEntities;
    @ManyToOne(fetch = FetchType.LAZY)
    private RoleEntity role;
}
