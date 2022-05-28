package org.sunbird.registration.service.impl;

import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.registration.model.UserProfile;
import org.sunbird.registration.service.RegistrationService;
import org.sunbird.workallocation.service.IndexerService;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RegistrationServiceImpl implements RegistrationService {

	private Logger LOGGER = LoggerFactory.getLogger(RegistrationServiceImpl.class);

	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	CbExtServerProperties serverProperties;

	@Autowired
	IndexerService indexerService;

	@Override
	public Boolean register(UserProfile userProfile) {
		try {
			String regCode = serverProperties.getUserRegistrationCode() + "-"
					+ (userProfile.getDeptName() != null ? userProfile.getDeptName() : "") + "-"
					+ RandomStringUtils.random(15, Boolean.TRUE, Boolean.TRUE);

			userProfile.setRegistrationCode(regCode);
			userProfile.setId(RandomStringUtils.random(15, Boolean.TRUE, Boolean.TRUE));
			userProfile.setStatus("INITIATE");

			RestStatus status = indexerService.addEntity(serverProperties.getUserRegistrationIndex(),
					serverProperties.getEsProfileIndexType(), userProfile.getId(),
					mapper.convertValue(userProfile, Map.class));
			if (status.equals(RestStatus.CREATED)) {
				return Boolean.TRUE;
			}
		} catch (Exception e) {
			LOGGER.error(String.format("Exception in %s : %s", "register", e.getMessage()));
		}
		return Boolean.FALSE;
	}

}
