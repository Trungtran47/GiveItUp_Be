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
    @Column(name = "name")
     String name;
    @Column(name = "name_slug")
     String nameSlug;
    @Column(name = "full_name")
     String fullName;
    @Column(name = "type")
     String type;
}
