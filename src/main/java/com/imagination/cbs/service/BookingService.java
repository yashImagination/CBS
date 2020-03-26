/**
 * 
 */
package com.imagination.cbs.service;

import org.springframework.data.domain.Page;

import com.imagination.cbs.dto.BookingDashBoardDto;
import com.imagination.cbs.dto.BookingDto;
import com.imagination.cbs.dto.BookingRequest;

/**
 * @author Ramesh.Suryaneni
 *
 */
public interface BookingService {
	public BookingDto addBookingDetails(BookingRequest booking);

	public BookingDto updateBookingDetails(Long bookingId, BookingRequest booking);

	public BookingDto processBookingDetails(Long bookingId, BookingDto booking);
	
	public Page<BookingDashBoardDto> getDraftOrCancelledBookings(String loggedInUser, String status, int pageNo, int pageSize);
}
