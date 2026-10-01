package org.ohdsi.webapi.source;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.ohdsi.webapi.arachne.encryption.EncryptorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Hibernate 6 AttributeConverter for encrypting/decrypting database string columns.
 * Replaces the legacy Hibernate custom type approach that extended AbstractEncryptedAsStringType.
 *
 * Uses Jasypt encryption with the defaultStringEncryptor bean configured in DataAccessConfig.
 *
 * Spring/Hibernate integration note:
 * By default, Hibernate instantiates @Converter classes via reflection (newInstance()),
 * bypassing Spring dependency injection entirely. DataAccessConfig registers a
 * SpringBeanContainer so Hibernate resolves the converter from the ApplicationContext, but
 * that is not guaranteed to run @Autowired either: in the GraalVM native image the setter
 * is never invoked and the encryptor stayed null, so encrypted ENC(...) credentials were
 * handed to the JDBC driver as-is. The encryptor is therefore held statically and set
 * directly by the defaultStringEncryptor bean, so every instance sees it however it was
 * created. A missing encryptor fails loudly instead of silently skipping encryption.
 */
@Component
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static volatile PBEStringEncryptor encryptor;

    public static void setDefaultEncryptor(PBEStringEncryptor defaultStringEncryptor) {
        encryptor = defaultStringEncryptor;
    }

    @Autowired
    public void setEncryptor(PBEStringEncryptor defaultStringEncryptor) {
        setDefaultEncryptor(defaultStringEncryptor);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return EncryptorUtils.encrypt(requireEncryptor(), attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return EncryptorUtils.decrypt(requireEncryptor(), dbData);
    }

    private static PBEStringEncryptor requireEncryptor() {
        PBEStringEncryptor current = encryptor;
        if (current == null) {
            throw new IllegalStateException("EncryptedStringConverter has no encryptor: the defaultStringEncryptor bean has not been created");
        }
        return current;
    }
}
