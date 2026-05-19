package com.syos.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.syos.domain.entity.Bill;
import com.syos.domain.valueobject.BillNumber;

public interface BillReadRepository {
    Optional<Bill> findByBillNumber(BillNumber billNumber);
    List<Bill> findAll();
    List<Bill> findByDate(LocalDate date);
    List<Bill> findBySaleType(Bill.SaleType saleType);
}
