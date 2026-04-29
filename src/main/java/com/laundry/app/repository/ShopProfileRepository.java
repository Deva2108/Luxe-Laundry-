package com.laundry.app.repository;

import com.laundry.app.model.ShopProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopProfileRepository extends JpaRepository<ShopProfile, Long> {
}
