package org.sunbird.registration.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.model.SunbirdApiResp;
import org.sunbird.registration.model.UserProfile;
import org.sunbird.registration.service.RegistrationService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/user")
public class RegistrationController {

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	RegistrationService registrationService;

	@PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
	public String register(@RequestBody UserProfile userProfile) throws JsonProcessingException {
		SunbirdApiResp sunbirdResp = new SunbirdApiResp();
		String validation = registerPayloadValidation(userProfile);
		if (StringUtils.isBlank(validation)) {
			Boolean response = registrationService.register(userProfile);
			sunbirdResp.setResponseCode(response ? "Success" : "Failed");
		} else {
			sunbirdResp.setResponseCode(validation);
		}
		return mapper.writeValueAsString(sunbirdResp);
	}

	private String registerPayloadValidation(UserProfile userProfile) {
		if (StringUtils.isBlank(userProfile.getFirstName())) {
			return "Firstname missing";
		}
		if (StringUtils.isBlank(userProfile.getLastName())) {
			return "Lastname missing";
		}
		if (StringUtils.isBlank(userProfile.getEmail())) {
			return "Email missing";
		}
		if (StringUtils.isBlank(userProfile.getDeptId())) {
			return "Department Id missing";
		}
		if (StringUtils.isBlank(userProfile.getDeptName())) {
			return "Department name missing";
		}
		if (StringUtils.isBlank(userProfile.getDesignation())) {
			return "Designation missing";
		}

		return StringUtils.EMPTY;

	}

}
