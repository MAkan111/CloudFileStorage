package ru.maakan111.cloudfilestorage.dto;

import lombok.Data;

@Data
public class ObjectMoveDto {
    private String oldObjectPath;
    private String newObjectPath;
}
