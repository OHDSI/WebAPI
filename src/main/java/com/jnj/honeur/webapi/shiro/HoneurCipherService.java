package com.jnj.honeur.webapi.shiro;

import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.util.ByteSource;

public class HoneurCipherService {

    private static AesCipherService cipher = new AesCipherService();
    private static byte[] keyBytes = new byte[] { -102, -45, -65, 117, -80, 1, 92, -23, 63, -26, -90, -32, -14, -61, 35, -31, -119, -10, -71, 121, -44, -43, 109, 35, -117, -79, -72, 126, -84, -8, -86, 0 };

    private HoneurCipherService() {}

    /**
     * Encrypts the given String
     * @param stringToEncrypt the string to encrypt
     * @return the encrypted base 64 encoded string
     */
    public static String encrypt(String stringToEncrypt) {
        byte[] secretBytes = CodecSupport.toBytes(stringToEncrypt);
        ByteSource encrypted = cipher.encrypt(secretBytes, keyBytes);
        return encrypted.toBase64();
    }

    /**
     * Decrypts the given encrypted bytes
     * @param encryptedBase64EncodedString the encrypted base 64 encoded string to decrypt
     * @return the decrypted String
     */
    public static String decrypt(String encryptedBase64EncodedString) {
        ByteSource decrypted = cipher.decrypt(Base64.decode(encryptedBase64EncodedString), keyBytes);
        return CodecSupport.toString(decrypted.getBytes());
    }

}
