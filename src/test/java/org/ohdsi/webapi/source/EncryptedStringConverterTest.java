package org.ohdsi.webapi.source;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;

public class EncryptedStringConverterTest {

    @After
    public void resetEncryptor() {
        EncryptedStringConverter.setDefaultEncryptor(null);
    }

    private static StandardPBEStringEncryptor encryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("test-password");
        return encryptor;
    }

    @Test
    public void converterCreatedWithoutInjectionDecrypts() {
        EncryptedStringConverter.setDefaultEncryptor(encryptor());

        // Hibernate may instantiate the converter itself, so @Autowired never runs on it.
        EncryptedStringConverter converter = new EncryptedStringConverter();
        String stored = converter.convertToDatabaseColumn("secret");

        assertThat(stored, startsWith("ENC("));
        assertThat(new EncryptedStringConverter().convertToEntityAttribute(stored), is("secret"));
    }

    @Test
    public void encryptedValueIsNotPassedThroughWithoutEncryptor() {
        String stored = "ENC(" + encryptor().encrypt("secret") + ")";

        assertThrows(IllegalStateException.class,
                () -> new EncryptedStringConverter().convertToEntityAttribute(stored));
    }

    @Test
    public void plaintextIsNotStoredWithoutEncryptor() {
        assertThrows(IllegalStateException.class,
                () -> new EncryptedStringConverter().convertToDatabaseColumn("secret"));
    }

    @Test
    public void autowiredSetterSharesEncryptorWithOtherInstances() {
        new EncryptedStringConverter().setEncryptor(encryptor());

        String stored = new EncryptedStringConverter().convertToDatabaseColumn("secret");

        assertThat(stored, not(is("secret")));
        assertThat(new EncryptedStringConverter().convertToEntityAttribute(stored), is("secret"));
    }
}
