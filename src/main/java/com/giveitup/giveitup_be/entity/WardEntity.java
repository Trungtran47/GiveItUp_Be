package com.giveitup.giveitup_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "wards")
public class WardEntity {
    @Id
    Integer id;
    @Column(name = "province_id")
    Integer provinceId;
    @Column(name = "name")
     String name;
    @Column(name = "slug")
     String slug;
    @Column(name = "type")
     String type;
    @Column(name = "name_with_type")
     String nameWithType;
    @Column(name = "path")
     String path;
    @Column(name = "path_with_type")
     String pathWithType;
}
