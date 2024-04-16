package ru.maakan111.cloudfilestorage.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FolderCreationDto {
    private String parentFolderPath;
    @Pattern(regexp = "^[\\w !.*+'\\[\\]()\\-]+$", message = "Folder name must not contain unsupported characters or non-English letters")
    private String newFolderName;
}
