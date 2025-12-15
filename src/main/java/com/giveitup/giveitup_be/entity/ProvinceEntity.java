package com.giveitup.giveitup_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "provinces")
public class ProvinceEntity {
    @Id
    Integer id;

    @Column(name = "name",columnDefinition = "NVARCHAR(MAX)")
     String name;
    @Column(name = "name_slug",columnDefinition = "NVARCHAR(MAX)")
     String nameSlug;
    @Column(name = "full_name",columnDefinition = "NVARCHAR(MAX)")
     String fullName;
    @Column(name = "type",columnDefinition = "NVARCHAR(MAX)")
     String type;
}
