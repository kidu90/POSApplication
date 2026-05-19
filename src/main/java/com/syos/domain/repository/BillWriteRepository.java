package com.syos.domain.repository;

import com.syos.domain.entity.Bill;

public interface BillWriteRepository {
    void save(Bill bill);
}
