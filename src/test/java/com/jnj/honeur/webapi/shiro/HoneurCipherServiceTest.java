package com.jnj.honeur.webapi.shiro;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HoneurCipherServiceTest {

    @Test
    public void encryptDecrypt() {
        String test = "test123";
        String encrypted = HoneurCipherService.encrypt(test);
        System.out.println(encrypted);
        String decrypted = HoneurCipherService.decrypt(encrypted);
        assertEquals(test, decrypted);
    }

    @Test
    public void encryptDecrypt2() {
        String test = "demo";
        String encrypted = HoneurCipherService.encrypt(test);
        System.out.println(encrypted);
    }

    @Test
    public void decrypt() {
        String decrypted = HoneurCipherService.decrypt("QieMjv79nhoz9S6g6LsA4MqCOryd18adLEybM6mEhbQ=");
        assertEquals("demo", decrypted);
    }

}