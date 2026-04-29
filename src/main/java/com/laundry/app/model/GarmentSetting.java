package com.laundry.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarmentSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String garmentName;
    
    @Enumerated(EnumType.STRING)
    private GarmentCategory defaultCategory;
    
    private Double defaultPrice;
}
