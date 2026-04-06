// Module: Official Campsite & Booking | Layer: Service Implementation
package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.domain.CampsiteBooking;
import tn.esprit.projetpidev.domain.CampsitePayment;
import tn.esprit.projetpidev.domain.enums.CampsitePaymentStatus;
import tn.esprit.projetpidev.dto.campsitepayment.CampsitePaymentRequest;
import tn.esprit.projetpidev.dto.campsitepayment.CampsitePaymentResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.CampsiteBookingRepository;
import tn.esprit.projetpidev.repositories.CampsitePaymentRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ICampsitePaymentServiceImpl implements ICampsitePaymentService {

    private final CampsitePaymentRepository paymentRepository;
    private final CampsiteBookingRepository bookingRepository;
    private final ICampsiteBookingService bookingService;

    @Override
    public CampsitePaymentResponse pay(CampsitePaymentRequest request) {
        CampsiteBooking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("CampsiteBooking", request.getBookingId()));

        if (paymentRepository.findByBooking_Id(booking.getId()).isPresent()) {
            throw new IllegalStateException("Payment already exists for this booking.");
        }

        CampsitePayment payment = CampsitePayment.builder()
                .booking(booking)
                .amount(request.getAmount())
                .method(request.getMethod())
                .transactionId(request.getTransactionId())
                .referenceCode(request.getReferenceCode())
                .status(CampsitePaymentStatus.PAID)
                .paidAt(LocalDateTime.now())
                .build();

        CampsitePayment saved = paymentRepository.save(payment);

        // Auto-confirm booking after payment
        bookingService.confirm(booking.getId());

        log.info("CampsitePayment recorded: id={}, booking={}", saved.getId(), booking.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CampsitePaymentResponse getByBooking(Long bookingId) {
        CampsitePayment payment = paymentRepository.findByBooking_Id(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for booking", bookingId));
        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public CampsitePaymentResponse getById(Long id) {
        return mapToResponse(paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CampsitePayment", id)));
    }

    @Override
    public CampsitePaymentResponse refund(Long id) {
        CampsitePayment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CampsitePayment", id));
        payment.setStatus(CampsitePaymentStatus.REFUNDED);
        log.info("CampsitePayment refunded: id={}", id);
        return mapToResponse(paymentRepository.save(payment));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    CampsitePaymentResponse mapToResponse(CampsitePayment p) {
        CampsitePaymentResponse r = new CampsitePaymentResponse();
        r.setId(p.getId());
        r.setBookingId(p.getBooking().getId());
        r.setAmount(p.getAmount());
        r.setTransactionId(p.getTransactionId());
        r.setReferenceCode(p.getReferenceCode());
        r.setMethod(p.getMethod());
        r.setStatus(p.getStatus());
        r.setPaidAt(p.getPaidAt());
        return r;
    }
}
