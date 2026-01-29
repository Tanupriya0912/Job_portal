package com.jobportal.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    @Value("${upload.dir:public/uploads/}")
    private String uploadDir;

    private static final String[] ALLOWED_EXTENSIONS = {"pdf", "doc", "docx"};
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public String uploadResume(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        validateFile(file);
        
        String fileName = generateFileName(getFileExtension(file.getOriginalFilename()));
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);
        
        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, file.getBytes());
        
        log.info("Resume uploaded successfully: {}", fileName);
        return "/uploads/" + fileName;
    }

    public void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        boolean isAllowed = false;
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (extension.equals(allowed)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new IllegalArgumentException("Only PDF, DOC, DOCX files allowed");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private String generateFileName(String extension) {
        return "resume-" + UUID.randomUUID() + "." + extension;
    }
}
