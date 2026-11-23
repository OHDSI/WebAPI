# Vendored data-source-manager

Source: https://github.com/OHDSI/ArachneCommons

Exact tag: `v1.17.4`; peeled commit: `5db47292077d4f016aece68295e3fabd48ad7bc1`.
Imported directory: `data-source-manager/` from the local read-only checkout
`/Users/konstantin/Projects/ArachneCommons` using `git show v1.17.4:<path>`.

Sources, tests, and resources were imported through apply_patch. Original license
headers are preserved; the upstream tag has no top-level LICENSE or NOTICE file.
Apache-2.0 license text is included in LICENSE. Files without upstream headers
retain their original contents. Source/resource EOF bytes are retained.

WebAPI modifications:
- Reparented the POM to org.ohdsi.webapi.security:security-libs:1.0.0;
  explicit artifact version 1.17.4-webapi1; Java 8 inherited from that parent.
- Removed upstream publishing configuration and use parent version properties.
- JUnit is 4.13.2 for tests.
- Preserved groupId com.odysseusinc and execution-engine-commons 1.17.4 (provided).
- Uses vendored arachne-common-utils 1.17.4-webapi1 and bcprov-jdk18on 1.87
  via the parent bouncycastle.version property.
- Spring and SLF4J are host-provided; encryption/runtime utility dependencies compile.
- KerberosServiceImpl uses ScalarTemplate; other production Java and all original
  resources remain unchanged. Added tests for real templates and service rendering.

Validation: compiled all production/test sources with javac --release 8 using cached
dependencies and ran JUnitCore: 17 tests passed across both modules. Maven was not
run; reactor integration is owned by the parent project.

## Original source manifest

SHA-256 hashes below describe original tag bytes, before the modifications above.
Reproduce any original with `git -C /Users/konstantin/Projects/ArachneCommons
show v1.17.4:<source-path>` (join this command onto one line).

```text
7bc9f9ba0ed2593066af30776d860e3a99ec861d39c261f6ebe4b8db470073e4  data-source-manager/pom.xml
8ea0ca559e7664a2c8e17d884194d898fa79b264b8316c2df8fdb7da16a2bced  data-source-manager/src/main/java/com/odysseusinc/datasourcemanager/encryption/EncryptionDecryptionService.java
c6e0186b954b938b7fc75fa0c19b6b98cd15ca063f3dd77475778ca2a3f697b2  data-source-manager/src/main/java/com/odysseusinc/datasourcemanager/encryption/EncryptorUtils.java
fbb26b03296c0de4deb82df8a0bd9acb9903a8c07d0d882e1abf5630dbb21ffb  data-source-manager/src/main/java/com/odysseusinc/datasourcemanager/encryption/NotEncrypted.java
5f35119bc2fcc93261baa9f37c7fbc1330343c57c79418313b6e7fdc9a88c49c  data-source-manager/src/main/java/com/odysseusinc/datasourcemanager/jdbc/DataSourceJdbcExecutor.java
7e97486d0273cdfc525fcdb18b19946ed42de010f9f440f667be59c9abf6348c  data-source-manager/src/main/java/com/odysseusinc/datasourcemanager/jdbc/JdbcTemplateConsumer.java
a14b58169620c17c0f4d6201e6370c10c89354cda713e6b826ad493b50fd0d0e  data-source-manager/src/main/java/com/odysseusinc/datasourcemanager/jdbc/auth/BigQueryAuthResolver.java
a95965468882a906a6e52b65b2966f391949d5b8d4ffaecb9972a8d0502c55b5  data-source-manager/src/main/java/com/odysseusinc/datasourcemanager/jdbc/auth/DataSourceAuthResolver.java
cc4396a236e28973ed694c871eadfe1d112b9f6737e92d1f91a4b9b0e156b936  data-source-manager/src/main/java/com/odysseusinc/datasourcemanager/jdbc/auth/KerberosAuthResolver.java
a6e8c537e9842ad65e4cedb065557589cf0318f63e2f14fc105282f16057399d  data-source-manager/src/main/java/com/odysseusinc/datasourcemanager/krblogin/CommandBuilder.java
b8429faa1fa77b57dd72c3cb74ee53019ae37a72cea0b3486cf468c61645d5ce  data-source-manager/src/main/java/com/odysseusinc/datasourcemanager/krblogin/KerberosService.java
45d9321ccb98b50a837a845fa51f7757c0f697ab0e0c05a20c6e2373652923fd  data-source-manager/src/main/java/com/odysseusinc/datasourcemanager/krblogin/KerberosServiceImpl.java
27a0b79268e871ee6dbf867270c501d04ba6d712bbd0a015d4fdec3fffd41456  data-source-manager/src/main/java/com/odysseusinc/datasourcemanager/krblogin/KinitComponents.java
42672f37304236b2d3376ff1c33cfd00bf48ce5f6e570f1437deb9d750bddb0b  data-source-manager/src/main/java/com/odysseusinc/datasourcemanager/krblogin/KrbConfig.java
43adda9397e8ed71e3d23b4263e326b93844070bb94f9739c490dc986b6caddf  data-source-manager/src/main/java/com/odysseusinc/datasourcemanager/krblogin/RuntimeServiceMode.java
d49a536cbe22dd1b3d140662c9b216a02a97b5fccd71c986766c0b0f350dd022  data-source-manager/src/main/resources/krb5.conf
c1b7babb2da036800d67661f2dc76228b84935292f395cdb8ae882447c9b9c79  data-source-manager/src/main/resources/templates/krb5ConfHeader.mustache
af6b74e36dc3ebcdc118d594460169ece41cb4de3deff8f7fcc2cb06ac1051a1  data-source-manager/src/main/resources/templates/krb5ConfRealm.mustache
c701697b02b9cacf4a46be82efc1661572123c4ee850c3373df60a6a2d4219d3  data-source-manager/src/test/java/com/odysseusinc/datasourcemanager/encryption/EncryptorUtilsTest.java
```
