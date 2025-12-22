package com.giveitup.giveitup_be.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "categories")
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(columnDefinition = "NVARCHAR(255)")
    String categoryName;
    @Column(columnDefinition = "NVARCHAR(MAX)")
    String description;
    Long projectCount;
    Long status;

    @CreatedDate //  Tự động set khi tạo mới
    @Column(updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate //  Tự động update khi có thay đổi
    LocalDateTime updatedAt;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<PostEntity> posts;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private Set<OrganizationEntity> organizations;

}
