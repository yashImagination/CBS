/**
 * 
 */
package com.imagination.cbs.service.impl;

import static com.imagination.cbs.util.AdobeConstant.FILE_EXTENSION;

import java.io.InputStream;
import java.net.URI;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imagination.cbs.constant.ApprovalStatusConstant;
import com.imagination.cbs.constant.SecurityConstants;
import com.imagination.cbs.constant.UserActionConstant;
import com.imagination.cbs.domain.Booking;
import com.imagination.cbs.domain.BookingRevision;
import com.imagination.cbs.dto.ApproveRequest;
import com.imagination.cbs.dto.BookingDto;
import com.imagination.cbs.dto.BookingRequest;
import com.imagination.cbs.exception.CBSApplicationException;
import com.imagination.cbs.exception.ResourceNotFoundException;
import com.imagination.cbs.mapper.BookingMapper;
import com.imagination.cbs.mapper.DisciplineMapper;
import com.imagination.cbs.mapper.TeamMapper;
import com.imagination.cbs.repository.BookingRepository;
import com.imagination.cbs.repository.BookingRevisionRepository;
import com.imagination.cbs.security.CBSUser;
import com.imagination.cbs.service.AdobeSignService;
import com.imagination.cbs.service.BookingService;
import com.imagination.cbs.service.EmailService;
import com.imagination.cbs.service.LoggedInUserService;
import com.imagination.cbs.service.helper.BookingApproveHelper;
import com.imagination.cbs.service.helper.BookingDeclineHelper;
import com.imagination.cbs.service.helper.BookingHrApproveHelper;
import com.imagination.cbs.service.helper.BookingSaveHelper;
import com.imagination.cbs.service.helper.CreateBookingHelper;
import com.imagination.cbs.service.helper.EmailHelper;
import com.imagination.cbs.util.AzureStorageUtility;

/**
 * @author Ramesh.Suryaneni
 *
 */

@Service("bookingService")
public class BookingServiceImpl implements BookingService {

	private static final Logger lOGGER = LoggerFactory.getLogger(BookingServiceImpl.class);

	private static final String BOOKING_NOT_FOUND_MESSAGE = "No Booking Available with this number : ";

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private BookingMapper bookingMapper;

	@Autowired
	private LoggedInUserService loggedInUserService;

	@Autowired
	private TeamMapper teamMapper;

	@Autowired
	private DisciplineMapper disciplineMapper;

	@Autowired
	private BookingRevisionRepository bookingRevisionRepository;

	@Autowired
	private AzureStorageUtility azureStorageUtility;

	@Autowired
	private AdobeSignService adobeSignService;

	@Autowired
	private BookingApproveHelper bookingApproveHelper;

	@Autowired
	private EmailHelper emailHelper;

	@Autowired
	private BookingSaveHelper bookingSaveHelper;

	@Autowired
	private BookingHrApproveHelper bookingHrApproveHelper;

	@Autowired
	private BookingDeclineHelper bookingDeclineHelper;

	@Autowired
	private CreateBookingHelper createBookingHelper;

	@Autowired
	private EmailService emailService;

	@Transactional
	@Override
	public BookingDto addBookingDetails(BookingRequest bookingRequest) { 
		Booking bookingDomain = createBookingHelper.populateBooking(bookingRequest, 1L, false);
		Booking savedBooking = bookingRepository.save(bookingDomain);
		return retrieveBookingDetails(savedBooking.getBookingId());
	}

	@Transactional
	@Override
	public BookingDto updateBookingDetails(Long bookingId, BookingRequest bookingRequest) { 
		Optional<Booking> booking = bookingRepository.findById(bookingId);
		if (booking.isPresent()) {
			Booking bookingDetails = booking.get();
			Long revisionNumber = bookingSaveHelper.getLatestRevision(bookingDetails).getRevisionNumber();
			Booking newBookingDomain = createBookingHelper.populateBooking(bookingRequest, ++revisionNumber, false);
			newBookingDomain.setBookingId(bookingId);
			newBookingDomain.setBookingDescription(bookingDetails.getBookingDescription());
			newBookingDomain.setChangedDate(bookingDetails.getChangedDate());
			bookingRepository.save(newBookingDomain);
			return retrieveBookingDetails(bookingId);
		} else {
			throw new ResourceNotFoundException(BOOKING_NOT_FOUND_MESSAGE + bookingId);
		}
	}

	@Transactional
	@Override
	public BookingDto submitBookingDetails(Long bookingId, BookingRequest bookingRequest) {
		Optional<Booking> booking = bookingRepository.findById(bookingId);
		if (booking.isPresent()) {
			Booking bookingDetails = booking.get();
			Long revisionNumber = bookingSaveHelper.getLatestRevision(bookingDetails).getRevisionNumber();
			Booking newBookingDomain = createBookingHelper.populateBooking(bookingRequest, ++revisionNumber, true);
			newBookingDomain.setBookingId(bookingId);
			newBookingDomain.setBookingDescription(bookingDetails.getBookingDescription());
			newBookingDomain.setChangedDate(new Timestamp(System.currentTimeMillis()));
			bookingRepository.save(newBookingDomain);
	 		BookingRevision latestRevision = bookingSaveHelper.getLatestRevision(newBookingDomain);
			// send mail to approver order #1
			emailHelper.prepareMailAndSend(newBookingDomain, latestRevision, 1L);
			return retrieveBookingDetails(bookingId);
		} else {
			throw new ResourceNotFoundException(BOOKING_NOT_FOUND_MESSAGE + bookingId);
		}
	}

	@Override
	public BookingDto retrieveBookingDetails(Long bookingId) {
		Optional<Booking> bookingDomain = bookingRepository.findById(bookingId);
		BookingDto bookingDto = null;
		if (bookingDomain.isPresent()) {
			Booking booking = bookingDomain.get();
			BookingRevision bookingRevision = bookingSaveHelper.getLatestRevision(booking);
			bookingDto = bookingMapper.convertToDto(bookingRevision);
			bookingDto.setTeam(teamMapper.toTeamDtoFromTeamDomain(booking.getTeam()));
			bookingDto.setBookingId(String.valueOf(booking.getBookingId()));
			bookingDto.setBookingDescription(booking.getBookingDescription());
			bookingDto.setInsideIr35(bookingRevision.getRole().getInsideIr35());
			bookingDto.setCreatedBy(booking.getChangedBy());
			bookingDto.setDiscipline(
					disciplineMapper.toDisciplineDtoFromDisciplineDomain(bookingRevision.getRole().getDiscipline()));
		} else {
			throw new ResourceNotFoundException(BOOKING_NOT_FOUND_MESSAGE + bookingId);
		}
		return bookingDto;
	}

	@Override
	@Transactional
	public BookingDto cancelBooking(Long bookingId) {

		String loggedInUser = loggedInUserService.getLoggedInUserDetails().getDisplayName();
		Optional<Booking> bookingDomain = bookingRepository.findById(bookingId);
		if (bookingDomain.isPresent()) {
			Booking booking = bookingDomain.get();
			if (booking.getChangedBy().equalsIgnoreCase(loggedInUser) && ApprovalStatusConstant.APPROVAL_DRAFT
					.getApprovalStatusId().equals(booking.getApprovalStatus().getApprovalStatusId())) {

				bookingRepository.delete(booking);

			} else if (booking.getChangedBy().equalsIgnoreCase(loggedInUser)) {

				BookingRevision latestBookingRevision = bookingSaveHelper.getLatestRevision(booking);

				bookingSaveHelper.saveBooking(booking, latestBookingRevision,
						ApprovalStatusConstant.APPROVAL_CANCELLED.getApprovalStatusId(), loggedInUserService.getLoggedInUserDetails());
				return retrieveBookingDetails(bookingId);
			}

		} else {
			throw new ResourceNotFoundException(BOOKING_NOT_FOUND_MESSAGE + bookingId);
		}
		return new BookingDto();
	}

	@Transactional
	@Override
	public BookingDto approveBooking(ApproveRequest request) {
		CBSUser user = loggedInUserService.getLoggedInUserDetails();
		Optional<Booking> bookingDomain = bookingRepository.findById(Long.valueOf(request.getBookingId()));
		if (bookingDomain.isPresent()) {
			Booking booking = bookingDomain.get();
			if (UserActionConstant.APPROVE.getAction().equalsIgnoreCase(request.getAction())) {

				bookingApproveHelper.approve(booking, user);

			} else if (UserActionConstant.HRAPPROVE.getAction().equalsIgnoreCase(request.getAction())) {

				bookingHrApproveHelper.hrApprove(booking);

			} else if (UserActionConstant.DECLINE.getAction().equalsIgnoreCase(request.getAction())) {
				bookingDeclineHelper.decline(booking);
			} else {
				throw new CBSApplicationException(
						"Request can't be processed, action should be anyone of APPROVE||HRAPPROVE||DECLINE");
			}

		} else {
			throw new CBSApplicationException(BOOKING_NOT_FOUND_MESSAGE + request.getBookingId());
		}
		return retrieveBookingDetails(Long.valueOf(request.getBookingId()));

	}

	@Override
	public void updateContract(String agreementId, String date) {

		// update contract signed details to booking
		BookingRevision bookingRevision = bookingRevisionRepository.findTopByAgreementIdOrderByChangedDateAsc(agreementId)
				.orElseThrow(() -> new ResourceNotFoundException("Agreement Id Not Found: " + agreementId));

		// download agreement from adobe
		InputStream pdfInputStream = adobeSignService.downloadAgreement(agreementId);

		// upload agreement to azure
		StringJoiner agreementName = new StringJoiner("-");
		agreementName.add(String.valueOf(bookingRevision.getBooking().getBookingId()));
		agreementName.add(bookingRevision.getJobNumber());
		agreementName.add(bookingRevision.getJobname());

		URI url = azureStorageUtility.uploadFile(pdfInputStream, agreementName + FILE_EXTENSION);

		lOGGER.info("Azure storage uri ::: {}", url);

		bookingRevision.setContractorSignedDate(new Timestamp(System.currentTimeMillis()));
		bookingRevision.setCompletedAgreementPdf(url.toString());
		bookingRevisionRepository.save(bookingRevision);

		// send email to creator/HR/?
		emailService.sendContractReceipt(bookingRevision);

	}

	@Override
	public List<BookingDto> retrieveBookingRevisions(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException(BOOKING_NOT_FOUND_MESSAGE + bookingId));
		return bookingMapper.convertToDtoList(booking.getBookingRevisions());
	}

	@Override
	public void sendBookingReminder(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException(BOOKING_NOT_FOUND_MESSAGE + bookingId));

		Long statusId = booking.getApprovalStatus().getApprovalStatusId();
		BookingRevision latestRevision = bookingSaveHelper.getLatestRevision(booking);
		if (ApprovalStatusConstant.APPROVAL_SENT_TO_HR.getApprovalStatusId().equals(statusId)) { 
			emailHelper.prepareMailAndSendToHR(latestRevision);
			return;
		}
		Long approverOrder = -1L;
		if (ApprovalStatusConstant.APPROVAL_1.getApprovalStatusId().equals(statusId)) {
			approverOrder = SecurityConstants.ROLE_APPROVER_ID.getRoleDetails();
		}

		if (ApprovalStatusConstant.APPROVAL_2.getApprovalStatusId().equals(statusId)) {
			approverOrder = SecurityConstants.ROLE_BOOKING_CREATOR_ID.getRoleDetails();
		}

		if (ApprovalStatusConstant.APPROVAL_3.getApprovalStatusId().equals(statusId)) {
			approverOrder = SecurityConstants.ROLE_BOOKING_VIEWER_ID.getRoleDetails();
		}
		emailHelper.prepareMailAndSend(booking, latestRevision, approverOrder);
	}
}
