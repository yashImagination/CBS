/**
 * 
 */
package com.imagination.cbs.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.imagination.cbs.dto.AdobeSignResponse;
import com.imagination.cbs.service.ConfigService;
import com.imagination.cbs.util.AdobeConstant;

/**
 * @author Ramesh.Suryaneni
 *
 */
@RestController
@RequestMapping(value = "/webhooks")
public class WebhookController {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	ConfigService configService;
	
	@PostMapping("/adobesign_callback")
	public String adobeSignCallback(@RequestHeader Map<String, String> headers) {
	    headers.forEach((key, value) -> {
	        System.out.println(String.format("Header '%s' = %s", key, value));
	    });
	    //TODO:implement contractor signed event to update in database for booking
	    return String.format("Listed %d headers", headers.size());
		
	}
	
	@GetMapping("/adobesign_callback")
	public AdobeSignResponse webhookVerification(@RequestHeader Map<String, String> headers) {
		AdobeSignResponse response = new AdobeSignResponse();
		
	    headers.forEach((key, value) -> {
	    	logger.info(String.format("Header '%s' = %s", key, value));
	    });
	    
	    // Fetch client id
	    String reqClientId = headers.getOrDefault("X-ADOBESIGN-CLIENTID", "");
	    String configCleintid = configService.getAdobeConfigs(AdobeConstant.ADOBE).getOrDefault(AdobeConstant.ADOBE_CLIENT_ID, "");
	    
	    // Validate it
	    if (reqClientId.equalsIgnoreCase(configCleintid))
	    		response.setXAdobeSignClientId(reqClientId);
	    
	    return response;
		
	}
	
}