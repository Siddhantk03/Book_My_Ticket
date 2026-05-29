package com.jsp.book.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsp.book.service.SeatAvailabilityService;
import com.jsp.book.service.SeatAvailabilityService.SeatAvailabilitySnapshot;
import com.jsp.book.service.SeatAvailabilityService.SeatLayoutStatus;
import com.jsp.book.service.SeatAvailabilityService.SeatStatus;

import com.jsp.book.service.TicketVerificationService;
import com.jsp.book.service.TicketVerificationService.TicketVerificationResult;
import com.jsp.book.service.TicketVerificationService.VerificationStats;

import com.jsp.book.util.RealTimeScanner;
import com.jsp.book.util.RealTimeScanner.QRData;

/**
 * REST API endpoints for scanner and seat availability
 */
@RestController
@RequestMapping("/api/scanner")
public class ScannerController {

    // ================= LOGGER =================

    private static final Logger log =
            LoggerFactory.getLogger(ScannerController.class);

    // ================= SERVICES =================

    private RealTimeScanner realTimeScanner;
    private TicketVerificationService ticketVerificationService;
    private SeatAvailabilityService seatAvailabilityService;

    // ================= CONSTRUCTOR =================

    public ScannerController(
            RealTimeScanner realTimeScanner,
            TicketVerificationService ticketVerificationService,
            SeatAvailabilityService seatAvailabilityService) {

        this.realTimeScanner = realTimeScanner;
        this.ticketVerificationService = ticketVerificationService;
        this.seatAvailabilityService = seatAvailabilityService;
    }

    // ==================== TICKET VERIFICATION ====================

    /**
     * Verify single ticket
     */
    @PostMapping("/verify")
    public ResponseEntity<TicketVerificationResult> verifyTicket(
            @RequestBody QRData qrData) {

        log.info("Received ticket verification request");

        String qrString =
                qrData.getMovieName() + " | " +
                qrData.getTheaterName() + " | " +
                qrData.getShowTiming() + " | " +
                qrData.getSeatNumbers();

        if (!realTimeScanner.isValidQRFormat(qrString)) {
            return ResponseEntity.badRequest().build();
        }

        TicketVerificationResult result =
                ticketVerificationService.verifyTicket(qrData);

        return ResponseEntity.ok(result);
    }

    /**
     * Bulk verify tickets
     */
    @PostMapping("/verify-bulk")
    public ResponseEntity<TicketVerificationResult[]> bulkVerify(
            @RequestBody QRData[] qrDataArray) {

        log.info("Bulk verifying {} tickets", qrDataArray.length);

        TicketVerificationResult[] results =
                ticketVerificationService.bulkVerify(qrDataArray);

        return ResponseEntity.ok(results);
    }

    /**
     * Get verification stats
     */
    @GetMapping("/stats")
    public ResponseEntity<VerificationStats> getStats() {

        log.info("Fetching verification statistics");

        VerificationStats stats =
                ticketVerificationService.getStats();

        return ResponseEntity.ok(stats);
    }

    // ==================== SEAT AVAILABILITY ====================

    /**
     * Get show availability
     */
    @GetMapping("/seats/{showId}/availability")
    public ResponseEntity<SeatAvailabilitySnapshot> getShowAvailability(
            @PathVariable Long showId) {

        log.info("Fetching availability for show {}", showId);

        SeatAvailabilitySnapshot snapshot =
                seatAvailabilityService.getShowAvailability(showId);

        if (snapshot == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(snapshot);
    }

    /**
     * Get all shows availability
     */
    @GetMapping("/seats/availability/all")
    public ResponseEntity<List<SeatAvailabilitySnapshot>>
            getAllShowsAvailability() {

        log.info("Fetching all shows availability");

        List<SeatAvailabilitySnapshot> snapshots =
                seatAvailabilityService.getAllShowsAvailability();

        return ResponseEntity.ok(snapshots);
    }

    /**
     * Get seat layout
     */
    @GetMapping("/seats/{showId}/layout")
    public ResponseEntity<SeatLayoutStatus> getSeatLayout(
            @PathVariable Long showId) {

        log.info("Fetching seat layout for show {}", showId);

        SeatLayoutStatus layout =
                seatAvailabilityService.getSeatLayout(showId);

        if (layout == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(layout);
    }

    /**
     * Get recent seat changes
     */
    @GetMapping("/seats/{showId}/changes")
    public ResponseEntity<List<SeatStatus>> getRecentChanges(
            @PathVariable Long showId,
            @RequestParam(required = false) Long since) {

        log.info("Fetching recent changes for show {}", showId);

        LocalDateTime lastCheck;

        if (since != null) {

            lastCheck = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(since),
                    java.time.ZoneId.systemDefault());

        } else {

            lastCheck = LocalDateTime.now().minusMinutes(1);
        }

        List<SeatStatus> changes =
                seatAvailabilityService.getRecentChanges(showId, lastCheck);

        return ResponseEntity.ok(changes);
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {

        return ResponseEntity.ok(
                "Scanner service is running successfully");
    }
}