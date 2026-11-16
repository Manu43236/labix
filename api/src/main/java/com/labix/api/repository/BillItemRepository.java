package com.labix.api.repository;

import com.labix.api.domain.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillItemRepository extends JpaRepository<BillItem, String> {

    List<BillItem> findByBillId(String billId);
}
