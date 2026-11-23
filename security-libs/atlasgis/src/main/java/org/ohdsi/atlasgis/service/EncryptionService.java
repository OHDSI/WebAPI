package org.ohdsi.atlasgis.service;

import com.odysseusinc.datasourcemanager.encryption.EncryptionDecryptionService;
import java.util.Objects;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService extends EncryptionDecryptionService {

	public EncryptionService(StringEncryptor encryptor) {

		super(encryptor);
	}
}
