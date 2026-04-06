// Module: Official Campsite & Booking | Layer: Domain Entity
package tn.esprit.projetpidev.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.esprit.projetpidev.domain.enums.CampsitePaymentMethod;
import tn.esprit.projetpidev.domain.enums.CampsitePaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "campsite_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "booking")
@EqualsAndHashCode(exclude = "booking")
public class CampsitePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private CampsiteBooking booking;

    @DecimalMin("0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    private String transactionId;
    private String referenceCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampsitePaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CampsitePaymentStatus status = CampsitePaymentStatus.PAID;

    private LocalDateTime paidAt;
}
