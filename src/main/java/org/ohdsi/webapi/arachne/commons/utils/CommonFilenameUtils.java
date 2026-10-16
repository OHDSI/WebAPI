package org.ohdsi.webapi.arachne.commons.utils;

import java.util.regex.Pattern;

/**
 * Utility class for sanitizing filenames to ensure they are valid across different operating systems.
 * 
 * Originally from com.odysseusinc.arachne.commons.utils
 */
public class CommonFilenameUtils {

    private static final Pattern WINDOWS_INVALID_CHARS = Pattern.compile("[\\\\/:*?\"<>|]");
    private static final Pattern POSIX_INVALID_CHARS = Pattern.compile("[/\\x00]");
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\x00-\\x1F\\x7F]");
    
    private CommonFilenameUtils() {
        // Utility class
    }

    /**
     * Sanitizes a filename to make it valid for most file systems (Windows, macOS, Linux).
     * Removes or replaces characters that are invalid in Windows filenames, which are the most restrictive.
     * 
     * @param filename the filename to sanitize
     * @return a sanitized filename safe for use on Windows, macOS, and Linux
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "unnamed";
        }
        
        // Replace Windows invalid characters with underscore
        String sanitized = WINDOWS_INVALID_CHARS.matcher(filename).replaceAll("_");
        
        // Remove control characters
        sanitized = CONTROL_CHARS.matcher(sanitized).replaceAll("");
        
        // Remove leading/trailing dots and spaces (problematic on Windows)
        sanitized = sanitized.replaceAll("^[.\\s]+", "");
        sanitized = sanitized.replaceAll("[.\\s]+$", "");
        
        // Handle reserved Windows filenames
        if (isReservedWindowsName(sanitized)) {
            sanitized = "_" + sanitized;
        }
        
        // If the filename is empty after sanitization, use a default
        if (sanitized.isEmpty()) {
            sanitized = "unnamed";
        }
        
        // Limit filename length (255 is common max for most filesystems)
        if (sanitized.length() > 255) {
            sanitized = sanitized.substring(0, 255);
        }
        
        return sanitized;
    }

    /**
     * Sanitizes a filename for POSIX-compliant file systems (Linux, macOS, Unix).
     * POSIX systems have fewer restrictions - mainly just forward slash and null characters.
     * 
     * @param filename the filename to sanitize
     * @return a sanitized filename safe for use on POSIX systems
     */
    public static String sanitizeFilenamePosix(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "unnamed";
        }
        
        // Replace POSIX invalid characters (forward slash and null) with underscore
        String sanitized = POSIX_INVALID_CHARS.matcher(filename).replaceAll("_");
        
        // Remove control characters for safety
        sanitized = CONTROL_CHARS.matcher(sanitized).replaceAll("");
        
        // Remove leading dots (hidden files on Unix/Linux)
        sanitized = sanitized.replaceAll("^\\.", "");
        
        // If the filename is empty after sanitization, use a default
        if (sanitized.isEmpty()) {
            sanitized = "unnamed";
        }
        
        // Limit filename length (255 is common for most POSIX filesystems)
        if (sanitized.length() > 255) {
            sanitized = sanitized.substring(0, 255);
        }
        
        return sanitized;
    }

    /**
     * Checks if a filename matches a reserved Windows name.
     * Reserved names: CON, PRN, AUX, NUL, COM1-9, LPT1-9
     * 
     * @param filename the filename to check
     * @return true if the filename is a reserved Windows name
     */
    private static boolean isReservedWindowsName(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        // Get the base name without extension
        String baseName = filename;
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = filename.substring(0, dotIndex);
        }
        
        String upperName = baseName.toUpperCase();
        
        // Check reserved names
        if (upperName.equals("CON") || upperName.equals("PRN") || 
            upperName.equals("AUX") || upperName.equals("NUL")) {
            return true;
        }
        
        // Check COM1-9 and LPT1-9
        if (upperName.matches("^(COM|LPT)[1-9]$")) {
            return true;
        }
        
        return false;
    }
}
