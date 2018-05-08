package org.ohdsi.webapi.source;

import org.jasypt.encryption.pbe.PBEStringEncryptor;

public class NotEncrypted implements PBEStringEncryptor {
    @Override
    public String encrypt(String message) {
        return message;
    }

    @Override
    public String decrypt(String encryptedMessage) {
        return encryptedMessage;
    }

    @Override
    public void setPassword(String password) {
    }
}
