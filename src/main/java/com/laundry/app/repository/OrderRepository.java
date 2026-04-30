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

    @Query("SELECT DISTINCT o FROM LaundryOrder o LEFT JOIN FETCH o.items WHERE o.status = :status ORDER BY o.createdAt DESC")
    List<LaundryOrder> findByStatusWithItems(@Param("status") OrderStatus status);
    
    @Query("SELECT DISTINCT o FROM LaundryOrder o LEFT JOIN FETCH o.items i WHERE " +
           "LOWER(o.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "o.phoneNumber LIKE CONCAT('%', :query, '%') OR " +
           "LOWER(i.garmentName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "CAST(i.category AS string) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY o.createdAt DESC")
    List<LaundryOrder> searchWithItems(@Param("query") String query);

    long countByIsPriority(boolean isPriority);
}
