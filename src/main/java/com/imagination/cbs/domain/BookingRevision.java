package com.imagination.cbs.domain;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;


/**
 * The persistent class for the booking_revision database table.
 * 
 */
@Entity
@Table(name="booking_revision")
@NamedQuery(name="BookingRevision.findAll", query="SELECT b FROM BookingRevision b")
public class BookingRevision implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="booking_revision_id")
	private long bookingRevisionId;

	@Column(name="agreement_document_id")
	private String agreementDocumentId;

	@Column(name="agreement_id")
	private String agreementId;

	@Column(name="approval_status_id")
	private long approvalStatusId;

	@Column(name="changed_by")
	private String changedBy;

	@Column(name="changed_date")
	private Timestamp changedDate;

	@Column(name="contracted_from_date")
	private Timestamp contractedFromDate;

	@Column(name="contracted_to_date")
	private Timestamp contractedToDate;

	@Column(name="contractor_contact_details")
	private String contractorContactDetails;

	@Column(name="contractor_employee_name")
	private String contractorEmployeeName;

	@Column(name="contractor_id")
	private long contractorId;

	@Column(name="contractor_name")
	private String contractorName;

	@Column(name="contractor_signed_date")
	private Timestamp contractorSignedDate;

	@Column(name="contractor_type")
	private String contractorType;

	@Column(name="employee_contact_details")
	private String employeeContactDetails;

	@Column(name="inside_ir35")
	private String insideIr35;

	@Column(name="job_number")
	private String jobNumber;

	@Column(name="known_as")
	private String knownAs;

	@Column(name="office_description")
	private String officeDescription;

	@Column(name="office_id")
	private String officeId;

	private BigDecimal rate;

	@Column(name="revision_number")
	private long revisionNumber;

	//bi-directional many-to-one association to Booking
	@ManyToOne
	@JoinColumn(name="booking_id")
	private Booking booking;

	//bi-directional one-to-one association to ContractorEmployeeRole
	@OneToOne
	@JoinColumn(name="contractor_employee_role_id")
	private ContractorEmployeeRole contractorEmployeeRole;

	//bi-directional one-to-one association to CurrencyDm
	@OneToOne
	@JoinColumn(name="currency_id")
	private CurrencyDm currencyDm;

	public BookingRevision() {
	}

	public long getBookingRevisionId() {
		return this.bookingRevisionId;
	}

	public void setBookingRevisionId(long bookingRevisionId) {
		this.bookingRevisionId = bookingRevisionId;
	}

	public String getAgreementDocumentId() {
		return this.agreementDocumentId;
	}

	public void setAgreementDocumentId(String agreementDocumentId) {
		this.agreementDocumentId = agreementDocumentId;
	}

	public String getAgreementId() {
		return this.agreementId;
	}

	public void setAgreementId(String agreementId) {
		this.agreementId = agreementId;
	}

	public long getApprovalStatusId() {
		return this.approvalStatusId;
	}

	public void setApprovalStatusId(long approvalStatusId) {
		this.approvalStatusId = approvalStatusId;
	}

	public String getChangedBy() {
		return this.changedBy;
	}

	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}

	public Timestamp getChangedDate() {
		return this.changedDate;
	}

	public void setChangedDate(Timestamp changedDate) {
		this.changedDate = changedDate;
	}

	public Timestamp getContractedFromDate() {
		return this.contractedFromDate;
	}

	public void setContractedFromDate(Timestamp contractedFromDate) {
		this.contractedFromDate = contractedFromDate;
	}

	public Timestamp getContractedToDate() {
		return this.contractedToDate;
	}

	public void setContractedToDate(Timestamp contractedToDate) {
		this.contractedToDate = contractedToDate;
	}

	public String getContractorContactDetails() {
		return this.contractorContactDetails;
	}

	public void setContractorContactDetails(String contractorContactDetails) {
		this.contractorContactDetails = contractorContactDetails;
	}

	public String getContractorEmployeeName() {
		return this.contractorEmployeeName;
	}

	public void setContractorEmployeeName(String contractorEmployeeName) {
		this.contractorEmployeeName = contractorEmployeeName;
	}

	public long getContractorId() {
		return this.contractorId;
	}

	public void setContractorId(long contractorId) {
		this.contractorId = contractorId;
	}

	public String getContractorName() {
		return this.contractorName;
	}

	public void setContractorName(String contractorName) {
		this.contractorName = contractorName;
	}

	public Timestamp getContractorSignedDate() {
		return this.contractorSignedDate;
	}

	public void setContractorSignedDate(Timestamp contractorSignedDate) {
		this.contractorSignedDate = contractorSignedDate;
	}

	public String getContractorType() {
		return this.contractorType;
	}

	public void setContractorType(String contractorType) {
		this.contractorType = contractorType;
	}

	public String getEmployeeContactDetails() {
		return this.employeeContactDetails;
	}

	public void setEmployeeContactDetails(String employeeContactDetails) {
		this.employeeContactDetails = employeeContactDetails;
	}

	public String getInsideIr35() {
		return this.insideIr35;
	}

	public void setInsideIr35(String insideIr35) {
		this.insideIr35 = insideIr35;
	}

	public String getJobNumber() {
		return this.jobNumber;
	}

	public void setJobNumber(String jobNumber) {
		this.jobNumber = jobNumber;
	}

	public String getKnownAs() {
		return this.knownAs;
	}

	public void setKnownAs(String knownAs) {
		this.knownAs = knownAs;
	}

	public String getOfficeDescription() {
		return this.officeDescription;
	}

	public void setOfficeDescription(String officeDescription) {
		this.officeDescription = officeDescription;
	}

	public String getOfficeId() {
		return this.officeId;
	}

	public void setOfficeId(String officeId) {
		this.officeId = officeId;
	}

	public BigDecimal getRate() {
		return this.rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public long getRevisionNumber() {
		return this.revisionNumber;
	}

	public void setRevisionNumber(long revisionNumber) {
		this.revisionNumber = revisionNumber;
	}

	public Booking getBooking() {
		return this.booking;
	}

	public void setBooking(Booking booking) {
		this.booking = booking;
	}

	public ContractorEmployeeRole getContractorEmployeeRole() {
		return this.contractorEmployeeRole;
	}

	public void setContractorEmployeeRole(ContractorEmployeeRole contractorEmployeeRole) {
		this.contractorEmployeeRole = contractorEmployeeRole;
	}

	public CurrencyDm getCurrencyDm() {
		return this.currencyDm;
	}

	public void setCurrencyDm(CurrencyDm currencyDm) {
		this.currencyDm = currencyDm;
	}

}