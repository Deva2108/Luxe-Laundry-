package com.laundry.app.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminInfoDTO {
    private String username;
    private String role;
    private String lastLogin;
    private String systemStatus;
}
