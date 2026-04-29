package com.laundry.app.repository;

import com.laundry.app.model.GarmentSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GarmentSettingRepository extends JpaRepository<GarmentSetting, Long> {
    Optional<GarmentSetting> findByGarmentNameIgnoreCase(String garmentName);
}
