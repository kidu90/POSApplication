package com.syos.presentation.gui;

import com.syos.application.strategy.DiscountStrategy;
import com.syos.application.strategy.FEFOStockSelectionStrategy;
import com.syos.application.strategy.StockSelectionStrategy;
import com.syos.application.strategy.ThresholdDiscountStrategy;
import com.syos.domain.entity.Bill;
import com.syos.domain.entity.User;
import com.syos.domain.valueobject.Money;

public class OnlineSalesFrame extends AbstractSalesFrame {
    private final DiscountStrategy discountStrategy = new ThresholdDiscountStrategy(new Money(1000), new Money(50));
    private final StockSelectionStrategy stockSelectionStrategy = new FEFOStockSelectionStrategy();

    public OnlineSalesFrame(GuiAppContext context, User user) {
        super(context, "Online Sales Management", "Online Sales Management - " + user.getFullName(), user.getFullName(), user.getAddress());
    }

    @Override
    protected Bill.SaleType getSaleType() {
        return Bill.SaleType.ONLINE;
    }

    @Override
    protected DiscountStrategy getDiscountStrategy() {
        return discountStrategy;
    }

    @Override
    protected StockSelectionStrategy getStockSelectionStrategy() {
        return stockSelectionStrategy;
    }
}