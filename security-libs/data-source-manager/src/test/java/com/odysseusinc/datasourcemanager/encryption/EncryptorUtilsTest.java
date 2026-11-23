package com.odysseusinc.datasourcemanager.encryption;

import static org.junit.Assert.*;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.Test;

public class EncryptorUtilsTest {

    private StringEncryptor encryptor= new DummyEncryptor();

    @Test
    public void encrypt_wrongValue() {
        assertEquals(null, EncryptorUtils.encrypt(encryptor, null));
    }

    @Test
    public void encrypt_wrongEncryptor() {
        assertEquals("value", EncryptorUtils.encrypt(null, "value"));
        assertEquals("value", EncryptorUtils.encrypt(new NotEncrypted(), "value"));
    }

    @Test
    public void encrypt() {
        String encrypt = EncryptorUtils.encrypt(encryptor, "value");
        assertEquals("ENC(value.encrypted)", encrypt);
    }

    @Test
    public void decrypt_wrongValue() {
        assertEquals(null, EncryptorUtils.decrypt(encryptor, null));
        assertEquals("value", EncryptorUtils.decrypt(encryptor, "value"));
    }

    @Test
    public void decrypt_wrongEncryptor() {
        assertEquals("value", EncryptorUtils.decrypt(null, "value"));
        assertEquals("value", EncryptorUtils.decrypt(new NotEncrypted(), "value"));
    }

    @Test
    public void decrypt() {
        String encrypt = EncryptorUtils.decrypt(encryptor, "ENC(value)");
        assertEquals("value.decrypted", encrypt);
    }


    private static class DummyEncryptor implements StringEncryptor {

        @Override
        public String encrypt(String s) {
            return String.format("%s.%s", s, "encrypted");
        }

        @Override
        public String decrypt(String s) {
            return String.format("%s.%s", s, "decrypted");
        }
    }

}