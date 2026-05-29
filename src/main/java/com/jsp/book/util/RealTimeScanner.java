package com.jsp.book.util;

import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Real-time Scanner for ticket validation at entrance
 * Reads ticket information from console/barcode scanner in real-time
 */
@Component
public class RealTimeScanner {

    // ================= LOGGER =================

    private static final Logger log =
            LoggerFactory.getLogger(RealTimeScanner.class);

    // ================= QR PATTERN =================

    private static final Pattern QR_PATTERN = Pattern.compile(
            "(.+?)\\s*\\|\\s*(.+?)\\s*\\|\\s*(.+?)\\s*\\|\\s*(.+)"
    );

    // ================= SCANNER =================

    private final Scanner inputScanner;

    public RealTimeScanner() {
        this.inputScanner = new Scanner(System.in);
    }

    /**
     * Reads QR code data from scanner/console in real-time
     * Expected format:
     * MovieName | TheaterName | ShowTiming | SeatNumbers
     */
    public String scanQRCode() {

        log.info("Waiting for QR code scan...");
        System.out.print("Scan ticket QR code or enter data: ");

        String qrData = null;

        if (inputScanner.hasNextLine()) {

            qrData = inputScanner.nextLine().trim();

            log.info("QR Code scanned: {}", qrData);
        }

        return qrData;
    }

    /**
     * Validates QR format
     */
    public boolean isValidQRFormat(String qrData) {

        if (qrData == null || qrData.isEmpty()) {
            return false;
        }

        return QR_PATTERN.matcher(qrData).matches();
    }

    /**
     * Parses QR code into QRData object
     */
    public QRData parseQRCode(String qrData) {

        var matcher = QR_PATTERN.matcher(qrData);

        if (!matcher.matches()) {

            log.warn("Invalid QR format: {}", qrData);

            return null;
        }

        QRData data = new QRData();

        data.setMovieName(matcher.group(1).trim());
        data.setTheaterName(matcher.group(2).trim());
        data.setShowTiming(matcher.group(3).trim());
        data.setSeatNumbers(matcher.group(4).trim());
        data.setScannedAt(LocalDateTime.now());

        return data;
    }

    /**
     * Continuous scanning mode
     */
    public String[] continuousScan() {

        log.info("Starting continuous QR scan mode");

        StringBuilder allScans = new StringBuilder();

        while (true) {

            System.out.print("\nScan QR (or 'exit'): ");

            String input = inputScanner.nextLine().trim();

            if ("exit".equalsIgnoreCase(input)) {

                log.info("Exiting scan mode");

                break;
            }

            if (isValidQRFormat(input)) {

                QRData data = parseQRCode(input);

                if (data != null) {

                    log.info(
                            "Scanned: {} - {} - {} - Seats: {}",
                            data.getMovieName(),
                            data.getTheaterName(),
                            data.getShowTiming(),
                            data.getSeatNumbers()
                    );
                }

                allScans.append(input).append("\n");

            } else {

                log.warn("Invalid format, try again");
            }
        }

        return allScans.toString().split("\n");
    }

    /**
     * Close scanner resource
     */
    public void close() {

        if (inputScanner != null) {

            inputScanner.close();

            log.info("Scanner closed");
        }
    }

    /**
     * Inner class to hold QR data
     */
    public static class QRData {

        private String movieName;
        private String theaterName;
        private String showTiming;
        private String seatNumbers;
        private LocalDateTime scannedAt;

        // ================= GETTERS AND SETTERS =================

        public String getMovieName() {
            return movieName;
        }

        public void setMovieName(String movieName) {
            this.movieName = movieName;
        }

        public String getTheaterName() {
            return theaterName;
        }

        public void setTheaterName(String theaterName) {
            this.theaterName = theaterName;
        }

        public String getShowTiming() {
            return showTiming;
        }

        public void setShowTiming(String showTiming) {
            this.showTiming = showTiming;
        }

        public String getSeatNumbers() {
            return seatNumbers;
        }

        public void setSeatNumbers(String seatNumbers) {
            this.seatNumbers = seatNumbers;
        }

        public LocalDateTime getScannedAt() {
            return scannedAt;
        }

        public void setScannedAt(LocalDateTime scannedAt) {
            this.scannedAt = scannedAt;
        }
    }
}