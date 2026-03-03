package com.example.calendar.DTO;

import com.example.calendar.model.SystemPermissionKey;
import lombok.Data;

@Data
public class SystemPermissionDefinitionUpdateDTO {
    public String definitionCode;
    public SystemPermissionKey permissionKey;
}
