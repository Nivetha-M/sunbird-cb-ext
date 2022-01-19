package org.sunbird.validation.service;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.validation.util.Base64Util;
import org.sunbird.validation.util.JsonKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ValidationService {

	private static CbExtLogger logger = new CbExtLogger(ValidationService.class.getName());
	private static final ObjectMapper mapper = new ObjectMapper();

	private static final String sso_url = "https://igot-dev.in/auth/";
	private static final String realm = "sunbird";

	public String verifyUserToken(String token) {
		String userId = JsonKey.UNAUTHORIZED;
		try {
			Map<String, Object> payload = validateToken(token);
			logger.debug("Access token validateToken() :" + payload.toString());
			if (MapUtils.isNotEmpty(payload) && checkIss((String) payload.get("iss"))) {
				userId = (String) payload.get(JsonKey.SUB);
				if (StringUtils.isNotBlank(userId)) {
					int pos = userId.lastIndexOf(":");
					userId = userId.substring(pos + 1);
				}
			}
		} catch (Exception ex) {
			logger.error(ex);
		}
		if (JsonKey.UNAUTHORIZED.equalsIgnoreCase(userId)) {
			logger.info("verifyUserAccessToken: Invalid User Token: " + token);
		}
		return userId;
	}

	private static Map<String, Object> validateToken(String token) throws JsonProcessingException {
		try {
			String[] tokenElements = token.split("\\.");
			String header = tokenElements[0];
			String body = tokenElements[1];
			String signature = tokenElements[2];
			String payLoad = header + JsonKey.DOT_SEPARATOR + body;

			Map<Object, Object> headerData = mapper.readValue(new String(decodeFromBase64(header)), Map.class);
			String keyId = headerData.get("kid").toString();
			boolean isValid = CryptoUtil.verifyRSASign(payLoad, decodeFromBase64(signature),
					KeyManager.getPublicKey(keyId).getPublicKey(), JsonKey.SHA_256_WITH_RSA);
			if (isValid) {
				Map<String, Object> tokenBody = mapper.readValue(new String(decodeFromBase64(body)), Map.class);
				boolean isExp = isExpired((Integer) tokenBody.get("exp"));
				if (isExp) {
					logger.info("Token is expired " + token);
					return Collections.emptyMap();
				}
				return tokenBody;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return Collections.emptyMap();
	}

	private static boolean checkIss(String iss) {
		String realmUrl = sso_url + "realms/" + realm;
		return (realmUrl.equalsIgnoreCase(iss));
	}

	private static boolean isExpired(Integer expiration) {
		return ((int) (new Date().getTime() / 1000) > expiration);
	}

	private static byte[] decodeFromBase64(String data) {
		return Base64Util.decode(data, 11);
	}

}
