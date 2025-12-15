package com.giveitup.giveitup_be.entity;

import com.giveitup.giveitup_be.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "username", unique = true, columnDefinition = "NVARCHAR(255)")
    String username;
    String password;
    @Column(columnDefinition = "NVARCHAR(255)")
    String firstName;
    @Column(columnDefinition = "NVARCHAR(255)")
    String lastName;
    LocalDate dob;
    String email;
    String phoneNumber;
    Long gender;
    @Column( columnDefinition = "NVARCHAR(255)")
    String address;
    String imageUser;
    String publicImageUserId;
    @Builder.Default
    @Column(nullable = false)
    boolean isPublic = false;


    @Column( columnDefinition = "NVARCHAR(MAX)")
    String introduce;
    Long status = (long) UserStatus.USER.getCode();
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    OrganizationEntity organization;
    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;
    @LastModifiedDate
    LocalDateTime updatedAt;
    @ManyToOne(fetch = FetchType.LAZY)
     RoleEntity role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<DonateEntity> donations = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<LikeEntity> likes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PostViewEntity> postViews = new ArrayList<>();

//    @OneToMany(mappedBy = "requestedBy", cascade = CascadeType.ALL)
//    List<PayoutEntity> payoutRequests = new ArrayList<>();

    @OneToMany(mappedBy = "createdByAdmin", cascade = CascadeType.ALL)
    List<PayoutEntity> payoutTransfers = new ArrayList<>();


    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FollowEntity> following = new ArrayList<>();

    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FollowEntity> followers = new ArrayList<>();

}
