package com.queueagent.repository;

import com.queueagent.entity.ProductStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OmsInventoryRepository extends JpaRepository<ProductStock, String> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductStock p " +
            "SET p.currentQuantity = p.currentQuantity + :quantity, " +
            "    p.updatedAt = :now " +
            "WHERE p.productCode = :productCode " +
            "AND p.currentQuantity + :quantity >= 0")
    int updateQuantity(
            @Param("productCode") String productCode,
            @Param("quantity") Integer quantity,
            @Param("now") LocalDateTime now
    );


}
