/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Alexander Saltykov, Vitaly Koulakov, Anton Gackovka, Alexandr Ryabokon, Mikhail Mironov
 * Created: Dec 1, 2017
 *
 */

/*
 * Hibernate 6 Migration Notes:
 *
 * Legacy Hibernate custom types (@TypeDef) have been replaced with JPA AttributeConverter.
 *
 * Encrypted String Fields:
 * - Old approach (Hibernate 4/5): @TypeDef with custom type extending AbstractEncryptedAsStringType
 * - New approach (Hibernate 6): @Convert(converter = EncryptedStringConverter.class)
 * - See: EncryptedStringConverter.java
 *
 * All encrypted fields in Source.java now use the EncryptedStringConverter AttributeConverter,
 * which uses Jasypt PBEStringEncryptor for encryption/decryption.
 */
package org.ohdsi.webapi.source;

