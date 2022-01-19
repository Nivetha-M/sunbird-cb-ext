package org.sunbird.validation.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.validation.service.ValidationService;

@RestController
@RequestMapping("/validate")
public class ValidationController {

	@Autowired
	ValidationService validationService;

	@GetMapping("/authentication")
	public String validateAuthToken(@RequestHeader("token") String token) {
		return validationService.verifyUserToken(token);

	}

}
