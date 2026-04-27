package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.domain.enums.CampsiteBookingStatus;
import tn.esprit.projetpidev.repositories.CampsiteBookingRepository;
import tn.esprit.projetpidev.repositories.CampsitePaymentRepository;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Admin analytics dashboard")
public class AnalyticsController {

    private final CampsiteBookingRepository bookingRepository;
    private final CampsitePaymentRepository paymentRepository;

    // ── Overview KPIs ────────────────────────────────────────────────────────

    @GetMapping("/overview")
    @Operation(summary = "KPI summary: total revenue, bookings, cancellations, fraud count")
    public ResponseEntity<Map<String, Object>> overview() {
        long totalBookings    = bookingRepository.count();
        long confirmed        = bookingRepository.countByStatus(CampsiteBookingStatus.CONFIRMED);
        long cancelled        = bookingRepository.countByStatus(CampsiteBookingStatus.CANCELLED);
        BigDecimal revenue    = paymentRepository.totalRevenue();
        long fraudCount       = bookingRepository.fraudSuspects(5).size();

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("totalRevenue",    revenue);
        kpis.put("totalBookings",   totalBookings);
        kpis.put("confirmedBookings", confirmed);
        kpis.put("cancelledBookings", cancelled);
        kpis.put("fraudSuspects",   fraudCount);
        return ResponseEntity.ok(kpis);
    }

    // ── Revenue by month ─────────────────────────────────────────────────────

    @GetMapping("/revenue-by-month")
    @Operation(summary = "Monthly revenue for the last 12 months")
    public ResponseEntity<List<Map<String, Object>>> revenueByMonth() {
        List<Object[]> rows = paymentRepository.revenueByMonth();
        List<Map<String, Object>> result = rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("month",   r[0]);
            m.put("revenue", r[1]);
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    // ── Occupancy per campsite ───────────────────────────────────────────────

    @GetMapping("/occupancy")
    @Operation(summary = "Booking stats per campsite")
    public ResponseEntity<List<Map<String, Object>>> occupancy() {
        List<Object[]> rows = bookingRepository.occupancyPerCampsite();
        List<Map<String, Object>> result = rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("campsiteId", r[0]);
            m.put("name",       r[1]);
            long total     = r[2] == null ? 0L : ((Number) r[2]).longValue();
            long confirmed = r[3] == null ? 0L : ((Number) r[3]).longValue();
            long cancelled = r[4] == null ? 0L : ((Number) r[4]).longValue();
            m.put("totalBookings",     total);
            m.put("confirmedBookings", confirmed);
            m.put("cancelledBookings", cancelled);
            m.put("cancellationRate",  total > 0 ? Math.round(cancelled * 100.0 / total) : 0);
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    // ── Fraud suspects ───────────────────────────────────────────────────────

    @GetMapping("/fraud-suspects")
    @Operation(summary = "Users with 5 or more cancelled bookings")
    public ResponseEntity<List<Map<String, Object>>> fraudSuspects(
            @RequestParam(defaultValue = "5") int minCancellations) {
        List<Object[]> rows = bookingRepository.fraudSuspects(minCancellations);
        List<Map<String, Object>> result = rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId",        r[0]);
            m.put("email",         r[1]);
            String firstname = r[2] != null ? r[2].toString() : "";
            String lastname  = r[3] != null ? r[3].toString() : "";
            m.put("fullName",      (firstname + " " + lastname).trim());
            m.put("cancellations", ((Number) r[4]).longValue());
            m.put("totalBookings", ((Number) r[5]).longValue());
            long cancellations = ((Number) r[4]).longValue();
            long total         = ((Number) r[5]).longValue();
            m.put("cancellationRate", total > 0 ? Math.round(cancellations * 100.0 / total) : 0);
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    // ── CSV Export ───────────────────────────────────────────────────────────

    @GetMapping("/export/csv")
    @Operation(summary = "Export all bookings as CSV")
    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"bookings-export.csv\"");

        List<Object[]> rows = bookingRepository.allBookingsForExport();

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Booking ID,Camper Email,Campsite,Check-in,Check-out,Guests,Total Price,Status,Created At");
            for (Object[] r : rows) {
                writer.printf("%s,%s,\"%s\",%s,%s,%s,%s,%s,%s%n",
                        r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7], r[8]);
            }
        }
    }
}
