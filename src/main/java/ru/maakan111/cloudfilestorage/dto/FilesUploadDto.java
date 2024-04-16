package ru.maakan111.cloudfilestorage.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import ru.maakan111.cloudfilestorage.validators.FilenamesPattern;

import java.util.List;

@Data
public class FilesUploadDto {
    private String parentFolderPath;
    @FilenamesPattern(regexp = "^([\\w !.*+\\[\\]'()\\-]+/)*[\\w !.*+\\[\\]'()\\-]+$",
            message = "File names must not contain unsupported characters or non-English letters")
    private List<MultipartFile> files;
}
