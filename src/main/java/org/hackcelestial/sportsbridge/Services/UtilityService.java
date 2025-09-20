package org.hackcelestial.sportsbridge.Services;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UtilityService {
    private static final String ROOT_FOLDER = "AppImages";
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    public String storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        File rootDir = new File(ROOT_FOLDER);
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }

        String uniqueId;
        File dir;
        do {
            uniqueId = generateRandomId(10);
            dir = new File(rootDir, uniqueId);
        } while (dir.exists());

        dir.mkdirs();

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "file";
        }
        Path targetFile = Path.of(dir.getAbsolutePath(), originalFilename);

        // Copy the file to the target location (replace if exists)
        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

        // Return web-accessible URL instead of absolute file path
        return "/images/" + uniqueId + "/" + originalFilename;
    }

    private String generateRandomId(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(ALPHANUMERIC.length());
            sb.append(ALPHANUMERIC.charAt(idx));
        }
        return sb.toString();
    }

    /**
     * Convert absolute file path to web-accessible URL
     * For existing images that were stored with absolute paths
     */
    public String convertFilePathToWebUrl(String absolutePath) {
        if (absolutePath == null || absolutePath.isEmpty()) {
            return "/images/placeholder/default.png"; // fallback image
        }

        // If it's already a web URL, return as is
        if (absolutePath.startsWith("/images/") || absolutePath.startsWith("http")) {
            return absolutePath;
        }

        // Convert absolute path like "F:\HCS\SB\SportsBridge\AppImages\KtfKlHnTkc\image.png"
        // to web URL like "/images/KtfKlHnTkc/image.png"
        try {
            String[] parts = absolutePath.replace("\\", "/").split("/");
            boolean foundAppImages = false;
            StringBuilder webUrl = new StringBuilder("/images");

            for (String part : parts) {
                if (foundAppImages && !part.isEmpty()) {
                    webUrl.append("/").append(part);
                }
                if ("AppImages".equals(part)) {
                    foundAppImages = true;
                }
            }

            return webUrl.length() > 7 ? webUrl.toString() : "/images/placeholder/default.png";
        } catch (Exception e) {
            return "/images/placeholder/default.png";
        }
    }
}
