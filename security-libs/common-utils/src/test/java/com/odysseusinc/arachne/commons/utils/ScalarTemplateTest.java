package com.odysseusinc.arachne.commons.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class ScalarTemplateTest {
    @Test
    public void substitutesOnceWithoutReplacementMetacharacters() throws Exception {
        Map<String, Object> values = new HashMap<>();
        values.put("name", "$1\\path {{other}}");
        values.put("other", "must not expand");
        assertEquals("[$1\\path {{other}}][$1\\path {{other}}]",
                new ScalarTemplate("[{{name}}][{{ name }}]").apply(values));
    }

    @Test
    public void missingAndNullValuesAreEmpty() throws Exception {
        Map<String, Object> values = new HashMap<>();
        values.put("nil", null);
        values.put("zero", 0);
        values.put("flag", false);
        assertEquals("||0|false", new ScalarTemplate("{{absent}}|{{nil}}|{{zero}}|{{flag}}")
                .apply(values));
        assertEquals("", new ScalarTemplate("{{name}}").apply((Object) null));
    }

    @Test
    public void preservesHandlebarsFourDefaultSevenCharacterEscaping() throws Exception {
        assertEquals("&lt;&gt;&quot;&#x27;&#x60;&amp;&#x3D;/ café 日本語 😀",
                new ScalarTemplate("{{name}}").apply(Collections.singletonMap("name", "<>\"'`&=/ café 日本語 😀")));
        assertEquals("<literal>&amp;", new ScalarTemplate("<literal>{{name}}")
                .apply(Collections.singletonMap("name", "&")));
    }

    @Test
    public void supportsOnlyMapOrNullContexts() throws Exception {
        ScalarTemplate template = new ScalarTemplate("{{name}}:{{missing}}:{{class}}");
        assertEquals("map::", template.apply(Collections.singletonMap("name", "map")));
        assertEquals("map::", template.apply((Object) Collections.singletonMap("name", "map")));
        assertEquals("::", template.apply((Object) null));
        assertEquals("::", template.apply((Map<String, ?>) null));
        for (Object context : new Object[] {new Bean(), "text", 42}) {
            for (ScalarTemplate candidate : new ScalarTemplate[] {template, new ScalarTemplate("literal")}) {
                try {
                    candidate.apply(context);
                    fail("Accepted a non-Map context");
                } catch (IllegalArgumentException expected) {
                    assertEquals("Template context must be null or a Map", expected.getMessage());
                }
            }
        }
    }

    public static class Bean {
        public String getName() { throw new AssertionError("Getter must never execute"); }
    }

    @Test
    public void rejectsMalformedAndUnsupportedSyntaxBeforeRendering() {
        String[] invalid = {"{{", "}}", "{{name", "{{}}", "{{{name}}}", "{{& name}}",
                "{{#if name}}yes{{/if}}", "{{!comment}}", "{{>partial}}", "{{helper name}}",
                "{{user.name}}", "{{../name}}", "{{@index}}", "{{name~}}", "{{=x x=}}",
                "{{lookup x 'y'}}", "{{name}}}}", "{{{{raw}}}}", "{{else}} {{/if}}"};
        for (String source : invalid) {
            try {
                new ScalarTemplate(source);
                fail("Accepted unsupported syntax: " + source);
            } catch (IllegalArgumentException expected) {
                // Fail closed rather than quietly producing incomplete configuration.
            }
        }
    }

    @Test
    public void loadsUtf8ClasspathResourceAndRejectsFilePaths() throws Exception {
        assertEquals("Olá 日本語 😀: café\n", TemplateUtils.loadTemplate("/unicode.mustache")
                .apply(Collections.singletonMap("name", "café")));
        for (String path : new String[] {"file:/etc/passwd", "/missing.mustache", "", null}) {
            try {
                TemplateUtils.loadTemplate(path);
                fail("Accepted invalid resource path: " + path);
            } catch (IllegalArgumentException expected) {
                assertNotNull(expected.getMessage());
            }
        }
    }
}
