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
 */
@Component
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static PBEStringEncryptor encryptor;

    @Autowired
    public void setEncryptor(PBEStringEncryptor defaultStringEncryptor) {
        EncryptedStringConverter.encryptor = defaultStringEncryptor;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return EncryptorUtils.encrypt(encryptor, attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return EncryptorUtils.decrypt(encryptor, dbData);
    }
}
