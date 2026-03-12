package com.queueagent.repository;

import com.queueagent.entity.TempStockRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempStockRequestRepository extends JpaRepository<TempStockRequest, Long> {
    Slice<TempStockRequest> findByProcessedFalse(Pageable pageable);
}
