/**
 * 
 */
package com.imagination.cbs.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.imagination.cbs.dto.BookingDashBoardDto;
import com.imagination.cbs.dto.BookingDto;
import com.imagination.cbs.dto.BookingRequest;
import com.imagination.cbs.exception.CBSValidationException;
import com.imagination.cbs.service.BookingService;

/**
 * @author Ramesh.Suryaneni
 *
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/bookings")
public class BookingController {

	@Autowired
	private BookingService bookingServiceImpl;

	@PostMapping(consumes = "application/json", produces = "application/json")
	public ResponseEntity<BookingDto> addBookingDetails(@RequestBody BookingRequest booking) {
		BookingDto draftBooking = bookingServiceImpl.addBookingDetails(booking);
		return new ResponseEntity<BookingDto>(draftBooking, HttpStatus.CREATED);
	}

	@PatchMapping(value = "/{booking_id}", consumes = "application/json", produces = "application/json")
	public ResponseEntity<BookingDto> updateBookingDetails(@PathVariable("booking_id") Long bookingId,
			@RequestBody BookingRequest booking) {
		BookingDto updatedBooking = bookingServiceImpl.updateBookingDetails(bookingId, booking);
		return new ResponseEntity<BookingDto>(updatedBooking, HttpStatus.OK);
	}

	@PutMapping(value = "/{booking_id}", consumes = "application/json", produces = "application/json")
	public ResponseEntity<BookingDto> processBookingDetails(@PathVariable("booking_id") Long bookingId,
			@Valid @RequestBody BookingRequest booking, BindingResult result) {
		if (result.hasErrors()) {
			throw new CBSValidationException(result.getFieldError().getDefaultMessage());
		}
		BookingDto processedBooking = bookingServiceImpl.processBookingDetails(bookingId, booking);
		return new ResponseEntity<BookingDto>(processedBooking, HttpStatus.OK);
	}

	@GetMapping()
	public Page<BookingDashBoardDto> getDraftedUserBookings(@RequestParam String status,
			@RequestParam(defaultValue = "0") Integer pageNo, @RequestParam(defaultValue = "5") Integer pageSize) {
		return bookingServiceImpl.getDraftOrCancelledBookings(status, pageNo, pageSize);
	}
}
