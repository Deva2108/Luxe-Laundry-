package com.laundry.app.service;

import com.laundry.app.dto.ShopProfileDTO;
import com.laundry.app.model.ShopProfile;
import com.laundry.app.repository.ShopProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ShopProfileRepository profileRepository;
    private static final Long DEFAULT_PROFILE_ID = 1L;

    @Transactional(readOnly = true)
    public ShopProfileDTO getProfile() {
        ShopProfile profile = profileRepository.findById(DEFAULT_PROFILE_ID)
                .orElse(ShopProfile.builder()
                        .id(DEFAULT_PROFILE_ID)
                        .shopName("LuxeLaundry")
                        .currencySymbol("₹")
                        .taxPercentage(0.0)
                        .build());
        return mapToDTO(profile);
    }

    @Transactional
    public ShopProfileDTO saveProfile(ShopProfileDTO dto) {
        ShopProfile profile = profileRepository.findById(DEFAULT_PROFILE_ID)
                .orElse(ShopProfile.builder().id(DEFAULT_PROFILE_ID).build());
        
        profile.setShopName(dto.getShopName());
        profile.setOwnerName(dto.getOwnerName());
        profile.setEmail(dto.getEmail());
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setAddress(dto.getAddress());
        profile.setTaxPercentage(dto.getTaxPercentage());
        profile.setCurrencySymbol(dto.getCurrencySymbol());
        
        return mapToDTO(profileRepository.save(profile));
    }

    private ShopProfileDTO mapToDTO(ShopProfile profile) {
        return ShopProfileDTO.builder()
                .shopName(profile.getShopName())
                .ownerName(profile.getOwnerName())
                .email(profile.getEmail())
                .phoneNumber(profile.getPhoneNumber())
                .address(profile.getAddress())
                .taxPercentage(profile.getTaxPercentage())
                .currencySymbol(profile.getCurrencySymbol())
                .build();
    }
}
