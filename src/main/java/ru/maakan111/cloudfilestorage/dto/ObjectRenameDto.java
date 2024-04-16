package ru.maakan111.cloudfilestorage.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

@Data
public class ObjectRenameDto implements Serializable {
    private String objectPath;
    @Pattern(regexp = "^[\\w !.*+'\\[\\]()\\-]+$", message = "Name must not contain unsupported characters or non-English letters")
    private String newName;
}
