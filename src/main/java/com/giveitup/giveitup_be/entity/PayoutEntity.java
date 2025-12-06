package com.giveitup.giveitup_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "payouts")
public class PayoutEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;


    @Column(nullable = false)
    Double amount;
    Double adminTransferAmount;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    String note;

    @Column(nullable = false)
    Long status; // Enum: PENDING, ADMIN_APPROVED, AUTHOR_CONFIRMED, TRANSFERRED, REJECTED, CANCELED

    // Kiểu: REQUEST (author tạo) / ADMIN_AUTO (admin tự trả)
    @Column(nullable = false)
    String type;

    // Author tạo request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by")
    UserEntity requestedBy;

    LocalDateTime requestedAt;

    // Lý do từ chối
    @Column(columnDefinition = "NVARCHAR(MAX)")
    String noteAdmin;
    String transferProofImageUrl;
    String transferProofImagePublicId;
    // Admin tạo payout (thực hiện chuyển khoản)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin")
    UserEntity createdByAdmin;
    LocalDateTime createdByAdminAt;

    // Author xác nhận đã nhận tiền
    LocalDateTime confirmedAt;

    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    PostEntity post;
    @OneToOne(mappedBy = "payout")
    PostUpdateEntity postUpdate;

}
