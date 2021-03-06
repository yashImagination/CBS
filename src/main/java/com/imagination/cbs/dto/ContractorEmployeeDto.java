package com.imagination.cbs.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ContractorEmployeeDto {

	private String contractorEmployeeId;

	private String changedBy;

	private String changedDate;

	private String contactDetails;

	private String employeeId;

	private String contractorEmployeeName;

	private String knownAs;

	private String status;
	
	private BigDecimal rate;
	
	private List<String> projects;
}
