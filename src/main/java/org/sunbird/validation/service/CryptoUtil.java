package org.sunbird.validation.service;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.validation.util.JsonKey;

public class CryptoUtil {

	private static final Charset US_ASCII = Charset.forName(JsonKey.US_ASCII);
	private static CbExtLogger logger = new CbExtLogger(CryptoUtil.class.getName());

	public static boolean verifyRSASign(String payLoad, byte[] signature, PublicKey key, String algorithm) {
		Signature sign;
		try {
			sign = Signature.getInstance(algorithm);
			sign.initVerify(key);
			sign.update(payLoad.getBytes(US_ASCII));
			return sign.verify(signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			logger.error(e);
			return false;
		}
	}

}
