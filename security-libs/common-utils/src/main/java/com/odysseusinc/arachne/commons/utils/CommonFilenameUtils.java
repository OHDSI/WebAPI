package com.odysseusinc.arachne.commons.utils;

import java.util.Objects;

public class CommonFilenameUtils {

    private static final String POSIX_RESERVED_CHARS = "[/\u0000]";
    private static final String WIN_ILLEGAL_CHARS = "[<>:\"/\\\\|?*\\u0000]";

    private CommonFilenameUtils() {}

    /**
     * Strict sanitization of given filename both valid for Windows and *nix systems.
     * <p></p>Filters most of characters enumerated by the Microsoft's
     * <a href="https://docs.microsoft.com/ru-ru/windows/win32/fileio/naming-a-file">Naming conventions</a>
     * </p>
     * @param filename
     * @return
     */
    public static String sanitizeFilename(String filename) {

        return Objects.requireNonNull(filename).replaceAll(WIN_ILLEGAL_CHARS,"");
    }

    /**
     * Sanitize filename according to POSIX filename limitations.
     * Reserved symbols are / and null
     * @param filename
     * @return
     */
    public static String sanitizeFilenamePosix(String filename) {

        return Objects.requireNonNull(filename).replaceAll(POSIX_RESERVED_CHARS, "");
    }
}
