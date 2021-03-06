package com.imagination.cbs.controller;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.imagination.cbs.config.TestConfig;
import com.imagination.cbs.dto.DisciplineDto;
import com.imagination.cbs.dto.RoleDto;
import com.imagination.cbs.security.GoogleAuthenticationEntryPoint;
import com.imagination.cbs.security.GoogleIDTokenValidationUtility;
import com.imagination.cbs.service.DisciplineService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(DisciplineController.class)
@ContextConfiguration(classes = {TestConfig.class})
public class DisciplineControllerTest {
	
	@MockBean
	private GoogleIDTokenValidationUtility googleIDTokenValidationUtility;

	@MockBean
	private GoogleAuthenticationEntryPoint googleAuthenticationEntryPoint;
	
	@MockBean
	private RestTemplateBuilder restTemplateBuilder;
	
	@Autowired
	private MockMvc mockMvc;
	

	@MockBean
	private DisciplineService disciplineService;
	
	
	@WithMockUser("developer")
	@Test
	public void shouldReturnListOfAllDisciplines() throws Exception {
		
		 List<DisciplineDto> listOfDisciplineDto = new ArrayList<DisciplineDto>();
		 listOfDisciplineDto.add(createDisciplineDto());
		
		 when(disciplineService.getAllDisciplines()).thenReturn(listOfDisciplineDto);
		
		 mockMvc.perform(get("/disciplines").contentType(MediaType.APPLICATION_JSON))
		 .andExpect(status().isOk())
		 .andExpect(jsonPath("$[0].disciplineName",comparesEqualTo("Creative")));
		
		 verify(disciplineService).getAllDisciplines();
	}

	@WithMockUser("developer")
	@Test
	public void shouldReturnListOfRoleBasedOnDisiplineId() throws Exception {

		 List<RoleDto> roleDtoList = new ArrayList<>();
		roleDtoList.add(createRoleDto());
		
		 when(disciplineService.findAllContractorRoles(8009l)).thenReturn(roleDtoList);
		
		 mockMvc.perform(get("/disciplines/8009/roles").contentType(MediaType.APPLICATION_JSON))
		 .andExpect(status().isOk()).
		 andExpect(jsonPath("$[0].roleName", comparesEqualTo("2D")));
		
		 verify(disciplineService).findAllContractorRoles(8009l);
	}

	private DisciplineDto createDisciplineDto() {
		DisciplineDto disciplineDto = new DisciplineDto();
		disciplineDto.setDisciplineId(8000);
		disciplineDto.setDisciplineName("Creative");
		disciplineDto.setDisciplineDescription("This is Creative");
		return disciplineDto;
	}
	
	private RoleDto createRoleDto(){
		RoleDto roleDto = new RoleDto();
		 roleDto.setStatus(null);
		 roleDto.setRoleName("2D");
		 roleDto.setRoleId("3214");
		 roleDto.setRoleDescription("2D");
		 roleDto.setRoleDefaultRate(null);
		 roleDto.setRoleCurrencyId(null);
		 roleDto.setInsideIr35("FALSE");
		 roleDto.setDisciplineId(null);
		 roleDto.setChangedDate("2020-03-17T08:02:03.767+05:30");
		 roleDto.setChangedBy("Akshay");	 
		 return roleDto;
	}
}
