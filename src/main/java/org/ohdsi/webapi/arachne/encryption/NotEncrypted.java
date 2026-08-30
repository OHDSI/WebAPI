package org.ohdsi.webapi.arachne.encryption;

import org.jasypt.encryption.pbe.PBEStringEncryptor;

public class NotEncrypted implements PBEStringEncryptor {
	@Override
	public String encrypt(String value) {

		return value;
	}

	@Override
	public String decrypt(String value) {

		return value;
	}

	@Override
	public void setPassword(String s) {
	}
}
