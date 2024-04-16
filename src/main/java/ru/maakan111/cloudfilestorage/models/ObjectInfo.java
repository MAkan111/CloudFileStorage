package ru.maakan111.cloudfilestorage.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.maakan111.cloudfilestorage.repositories.ObjectPath;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ObjectInfo {
    private ObjectPath objectPath;
    private long size;
    private LocalDateTime lastModified;


}
