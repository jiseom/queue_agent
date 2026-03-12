package com.queueagent.repository;

import com.queueagent.entity.InventoryOutbox;
import com.queueagent.enums.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface InventoryOutboxRepository extends JpaRepository<InventoryOutbox, Long> {
    Slice<InventoryOutbox> findByStatus(OutboxStatus status, Pageable pageable);

}
