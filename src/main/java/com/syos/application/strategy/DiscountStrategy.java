package com.syos.application.strategy;

import com.syos.domain.entity.Bill;
import com.syos.domain.valueobject.Money;

public interface DiscountStrategy {
    Money calculateDiscount(Bill bill);
    String getDescription();
}
