package com.laundry.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String garmentName;
    private Integer quantity;
    private Double pricePerItem;

    @Enumerated(EnumType.STRING)
    private GarmentCategory category;

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private LaundryOrder laundryOrder;
}
