# Vendored arachne-common-utils

Source: https://github.com/OHDSI/ArachneCommons

Exact tag: `v1.17.4`; peeled commit: `5db47292077d4f016aece68295e3fabd48ad7bc1`.
Imported directory: `arachne-common-utils/` from the local read-only checkout
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
- Replaced Handlebars with owned ScalarTemplate and classpath-only UTF-8 loading.
- Added scalar substitution tests and a Unicode resource.

ScalarTemplate supports only `{{name}}` (ASCII identifiers, optional whitespace).
Map keys are exact; Object contexts accept only null or Map, rejecting other types
without reflection or getter execution. Missing/null
values become empty strings. Values are escaped once using Handlebars 4.2.0 HBS4
entities for `< > " ' ` & =`: `&lt; &gt; &quot; &#x27; &#x60; &amp; &#x3D;`.
These entities were verified against the cached upstream 4.2.0 EscapingStrategy
bytecode. Replacement values are never parsed as templates. Helpers, blocks,
partials, comments, paths, raw output and malformed delimiters are rejected.
No filesystem loader or InputStream loading API is exposed. `apply(Object)` and
`apply(Map<String, ?>)` retain IOException in their signatures for callers.

Validation: compiled all production/test sources with javac --release 8 using cached
dependencies and ran JUnitCore: 17 tests passed across both modules. Maven was not
run; reactor integration is owned by the parent project.

## Original source manifest

SHA-256 hashes below describe original tag bytes, before the modifications above.
Reproduce any original with `git -C /Users/konstantin/Projects/ArachneCommons
show v1.17.4:<source-path>` (join this command onto one line).

```text
f2664fc32284e8420543458e723b27af73998864eb63185e5a98ee8e09a92519  arachne-common-utils/pom.xml
6f350f21a30cfd0317e4c9c327925637258f9a62a7f5b28d6a9ae5d2853d4300  arachne-common-utils/src/main/java/com/odysseusinc/arachne/commons/utils/AnnotationReflectionUtils.java
818cb1e05fe02a2e1d53e915f894ad0dfb1653aa80707f6b80dec8781aac9b2f  arachne-common-utils/src/main/java/com/odysseusinc/arachne/commons/utils/CommonFilenameUtils.java
fb273571bf17e9a8401f31dbd5c27c75594eaad51467f2dfe00a509e26556700  arachne-common-utils/src/main/java/com/odysseusinc/arachne/commons/utils/QuoteUtils.java
5c74e08700fa1e3344ebcdf32edf2d4314481edfd46f3ff617848d7506f9b1f2  arachne-common-utils/src/main/java/com/odysseusinc/arachne/commons/utils/TempHolder.java
6e565ad197efe26819d76fa5f4f018ff26dcecef1c1023ff281949503c664710  arachne-common-utils/src/main/java/com/odysseusinc/arachne/commons/utils/TemplateUtils.java
05ef67798f9a3394e7af881afabb7cea45538ca7674d029df83a460f8dd0024d  arachne-common-utils/src/main/java/com/odysseusinc/arachne/commons/utils/UUIDGenerator.java
97751b17df771bbdcd972785f2e93a250948ceaefbacc7dcf54bab148d60f75b  arachne-common-utils/src/main/java/com/odysseusinc/arachne/commons/utils/ZipUtils.java
9eaee96a543adf01e8022b6614c838e35ac4bd3f25beb8db451b708ad0fbd153  arachne-common-utils/src/test/java/com/odysseusinc/arachne/commons/utils/CommonFilenameUtilsTest.java
1797bcd25b57fd413ec0e09464a0954ad23cf3faac61c4391fbd88de748cb3dd  arachne-common-utils/src/test/java/com/odysseusinc/arachne/commons/utils/TemplateUtilsTest.java
ddd244c65592ab6e7d6db2512d381131a0f4812863d4c2e8ae245622ca3c1678  arachne-common-utils/src/test/resources/test.handlebars
```
