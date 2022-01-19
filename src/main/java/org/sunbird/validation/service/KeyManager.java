package org.sunbird.validation.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.validation.model.KeyData;
import org.sunbird.validation.util.Base64Util;

public class KeyManager {

	private static final CbExtLogger logger = new CbExtLogger(KeyManager.class.getName());
	private static final Map<String, KeyData> keyMap = new HashMap<>();
	private static KeyManager instance = getInstance();
	private static final String RESOURCE_LOCATION = "classpath*:keys/*";

	@Autowired
	ResourceLoader resourceLoader;

	private KeyManager() {
		try {
			Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
					.getResources(RESOURCE_LOCATION);
			for (Resource resource : resources) {
				InputStream is = resource.getInputStream();
				byte[] encoded = IOUtils.toByteArray(is);
				String content = new String(encoded, Charset.forName("UTF-8"));
				KeyData keyData = null;
				try {
					keyData = new KeyData(resource.getFilename(), loadPublicKey(content));
				} catch (Exception e) {
					logger.error(e);
				}
				keyMap.put(resource.getFilename(), keyData);
			}
		} catch (IOException e) {
			logger.error(e);

		}
	}

	public static KeyManager getInstance() {
		if (instance == null) {
			// To make thread safe
			synchronized (KeyManager.class) {
				// check again as multiple threads
				// can reach above step
				if (instance == null)
					instance = new KeyManager();
			}
		}
		return instance;

	}

	public static KeyData getPublicKey(String keyId) {
		return keyMap.get(keyId);
	}

	public static PublicKey loadPublicKey(String key) throws Exception {
		String publicKey = new String(key.getBytes(), StandardCharsets.UTF_8);
		publicKey = publicKey.replaceAll("(-+BEGIN PUBLIC KEY-+)", "");
		publicKey = publicKey.replaceAll("(-+END PUBLIC KEY-+)", "");
		publicKey = publicKey.replaceAll("[\\r\\n]+", "");
		byte[] keyBytes = Base64Util.decode(publicKey.getBytes("UTF-8"), Base64Util.DEFAULT);

		X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(X509publicKey);
	}

}
