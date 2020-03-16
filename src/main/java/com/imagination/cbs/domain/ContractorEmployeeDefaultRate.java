package com.imagination.cbs.domain;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;


/**
 * The persistent class for the contractor_employee_default_rate database table.
 * 
 */
@Entity
@Table(name="contractor_employee_default_rate")
@NamedQuery(name="ContractorEmployeeDefaultRate.findAll", query="SELECT c FROM ContractorEmployeeDefaultRate c")
public class ContractorEmployeeDefaultRate implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="rate_id")
	private long rateId;

	@Column(name="changed_by")
	private String changedBy;

	@Column(name="changed_date")
	private Timestamp changedDate;

	@Column(name="date_from")
	private Timestamp dateFrom;

	private BigDecimal rate;

	//bi-directional one-to-one association to ContractorEmployee
	@OneToOne
	@JoinColumn(name="contractor_employee_id")
	private ContractorEmployee contractorEmployee;

	//bi-directional one-to-one association to CurrencyDm
	@OneToOne
	@JoinColumn(name="currency_id")
	private CurrencyDm currencyDm;

	public ContractorEmployeeDefaultRate() {
	}

	public long getRateId() {
		return this.rateId;
	}

	public void setRateId(long rateId) {
		this.rateId = rateId;
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

	public Timestamp getDateFrom() {
		return this.dateFrom;
	}

	public void setDateFrom(Timestamp dateFrom) {
		this.dateFrom = dateFrom;
	}

	public BigDecimal getRate() {
		return this.rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public ContractorEmployee getContractorEmployee() {
		return this.contractorEmployee;
	}

	public void setContractorEmployee(ContractorEmployee contractorEmployee) {
		this.contractorEmployee = contractorEmployee;
	}

	public CurrencyDm getCurrencyDm() {
		return this.currencyDm;
	}

	public void setCurrencyDm(CurrencyDm currencyDm) {
		this.currencyDm = currencyDm;
	}

}