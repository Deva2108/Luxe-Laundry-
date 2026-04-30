package com.laundry.app.controller;

import com.laundry.app.dto.ShopProfileDTO;
import com.laundry.app.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ShopProfileDTO> getProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }

    @PostMapping
    public ResponseEntity<ShopProfileDTO> updateProfile(@jakarta.validation.Valid @RequestBody ShopProfileDTO dto) {
        log.info("Updating shop profile settings for: {}", dto.getShopName());
        return ResponseEntity.ok(profileService.saveProfile(dto));
    }

    @GetMapping("/admin/info")
    public ResponseEntity<com.laundry.app.dto.AdminInfoDTO> getAdminInfo() {
        return ResponseEntity.ok(com.laundry.app.dto.AdminInfoDTO.builder()
                .username("admin")
                .role("SYSTEM_ADMIN")
                .lastLogin(java.time.LocalDateTime.now().minusMinutes(2).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .systemStatus("HEALTHY")
                .build());
    }
}
