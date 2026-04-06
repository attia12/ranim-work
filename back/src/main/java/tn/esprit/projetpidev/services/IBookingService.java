package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.dto.booking.BookingRequest;
import tn.esprit.projetpidev.dto.booking.BookingResponse;

import java.util.List;

public interface IBookingService {

    BookingResponse createBooking(BookingRequest request);

    BookingResponse getBookingById(Long bookingId);

    List<BookingResponse> getBookingsByUser(Long userId);

    List<BookingResponse> getBookingsByEvent(Long eventId);

    BookingResponse updateStatus(Long bookingId, String status);

    void cancelBooking(Long bookingId);
}
