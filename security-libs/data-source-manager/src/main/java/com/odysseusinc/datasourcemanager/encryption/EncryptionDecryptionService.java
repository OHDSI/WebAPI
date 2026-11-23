package com.odysseusinc.datasourcemanager.encryption;

import org.jasypt.encryption.StringEncryptor;

public class EncryptionDecryptionService {
	private final StringEncryptor encryptor;

	public EncryptionDecryptionService(StringEncryptor encryptor) {
		this.encryptor = encryptor;
	}

	public String decrypt(String value) {
		return EncryptorUtils.decrypt(this.encryptor, value);
	}

	public String encrypt(String value) {
		return EncryptorUtils.encrypt(this.encryptor, value);
	}

}
