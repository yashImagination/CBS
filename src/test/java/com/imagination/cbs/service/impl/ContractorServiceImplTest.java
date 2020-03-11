package com.imagination.cbs.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.imagination.cbs.domain.Contractor;
import com.imagination.cbs.dto.ContractorDto;
import com.imagination.cbs.mapper.ContractorMapper;
import com.imagination.cbs.repository.ContractorRepository;

@RunWith(MockitoJUnitRunner.class)
public class ContractorServiceImplTest {

	@Mock
	ContractorRepository repository;
	
	@Mock
	ContractorMapper mapper;
	
	@InjectMocks
	ContractorServiceImpl serviceImpl;
	
	@Test
	public void getContractorsContainingName_NameExists() {
		List<Contractor> contractorList = new ArrayList<>();
		
		Contractor cntr1 = new Contractor();
		cntr1.setContractorId(101);
		cntr1.setContractorName("Yash");
		Contractor cntr2 = new Contractor();
		cntr2.setContractorId(102);
		cntr2.setContractorName("Yash Technologies");
		contractorList.add(cntr1);
		contractorList.add(cntr2);
		
		List<ContractorDto> contractorDtoList = new ArrayList<>();
		
		ContractorDto cntrDto1 = new ContractorDto();
		cntrDto1.setContractorId(101);
		cntrDto1.setContractorName("Yash");
		ContractorDto cntrDto2 = new ContractorDto();
		cntrDto2.setContractorId(102);
		cntrDto2.setContractorName("Yash Technologies");
		
		contractorDtoList.add(cntrDto1);
		contractorDtoList.add(cntrDto2);
		when(repository.findByContractorNameContains(Mockito.anyString())).thenReturn(contractorList);
		when(mapper.toListContractorDto(Mockito.any())).thenReturn(contractorDtoList);
		List<ContractorDto> resultContractorDtoList = serviceImpl.getContractorsContainingName("As");
		
		assertEquals(resultContractorDtoList.get(0).getContractorName(), "Yash");
		assertEquals(resultContractorDtoList.get(1).getContractorName(), "Yash Technologies");
	}
	
	@Test
	public void getContractorsContainingName_NoNameExists() {
		List<ContractorDto> contractorDtoList = new ArrayList<>();

		when(mapper.toListContractorDto(Mockito.any())).thenReturn(contractorDtoList);
		
		List<ContractorDto> resultContractorDtoList = serviceImpl.getContractorsContainingName("As");
		
		assertEquals(contractorDtoList, resultContractorDtoList);
	}
}
