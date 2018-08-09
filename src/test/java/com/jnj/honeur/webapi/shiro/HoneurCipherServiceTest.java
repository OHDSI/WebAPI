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

}