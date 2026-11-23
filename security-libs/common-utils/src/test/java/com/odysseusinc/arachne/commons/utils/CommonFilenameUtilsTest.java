package com.odysseusinc.arachne.commons.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import org.hamcrest.Matcher;
import org.junit.Test;

public class CommonFilenameUtilsTest {

    private static final String testFilename = "filename?\"containing\"/illegal<:>ch\\ars:)";
    private static final String expectFilename = "filenamecontainingillegalchars)";
    private static final String posixTestFilename = "another:name|to/test";
    private static final String posixExpectName = "another:name|totest";

    @Test
    public void sanitizeFilename() {

        String strictName = CommonFilenameUtils.sanitizeFilename(testFilename);
        assertThat(strictName, is(equalTo(expectFilename)));

        String posixName = CommonFilenameUtils.sanitizeFilenamePosix(posixTestFilename);
        assertThat(posixName, is(equalTo(posixExpectName)));
    }
}