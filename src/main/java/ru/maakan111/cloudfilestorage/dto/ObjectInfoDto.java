package ru.maakan111.cloudfilestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ObjectInfoDto {
    private String name;
    private boolean isFolder;
    private String path;
    private String size;
    private LocalDateTime lastModified;
}
