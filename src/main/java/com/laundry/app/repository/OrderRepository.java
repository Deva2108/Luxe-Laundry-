package com.laundry.app.repository;

import com.laundry.app.model.LaundryOrder;
import com.laundry.app.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<LaundryOrder, Long> {
    
    @Query("SELECT o FROM LaundryOrder o LEFT JOIN FETCH o.items WHERE o.orderId = :orderId")
    Optional<LaundryOrder> findByOrderId(@Param("orderId") String orderId);

    @Query("SELECT DISTINCT o FROM LaundryOrder o LEFT JOIN FETCH o.items ORDER BY o.createdAt DESC")
    List<LaundryOrder> findAllWithItems();

    List<LaundryOrder> findByStatus(OrderStatus status);
    
    @Query("SELECT o FROM LaundryOrder o WHERE LOWER(o.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR o.phoneNumber LIKE CONCAT('%', :query, '%')")
    List<LaundryOrder> search(@Param("query") String query);

    long countByIsPriority(boolean isPriority);

    @Query("SELECT DISTINCT i.garmentName FROM OrderItem i")
    List<String> findDistinctGarmentNames();
}
