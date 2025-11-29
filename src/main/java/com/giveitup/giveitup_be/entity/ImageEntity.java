package com.giveitup.giveitup_be.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "post_images")
@EntityListeners(AuditingEntityListener.class)
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    // Đường dẫn ảnh (URL từ cloud)
    String publicId;
    @Column(nullable = false, columnDefinition = "NVARCHAR(500)")
    String imageUrl;
    // true = ảnh đại diện (thumbnail), false = ảnh thường
    @Column(nullable = false)
    Boolean isThumbnail = false;
    // Mối quan hệ với Post
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonBackReference
    PostEntity post;
}
