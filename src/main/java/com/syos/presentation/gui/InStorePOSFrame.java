package com.syos.presentation.gui;

import com.syos.application.strategy.DiscountStrategy;
import com.syos.application.strategy.FEFOStockSelectionStrategy;
import com.syos.application.strategy.NoDiscountStrategy;
import com.syos.application.strategy.StockSelectionStrategy;
import com.syos.domain.entity.Bill;

public class InStorePOSFrame extends AbstractSalesFrame {
    private final DiscountStrategy discountStrategy = new NoDiscountStrategy();
    private final StockSelectionStrategy stockSelectionStrategy = new FEFOStockSelectionStrategy();

    public InStorePOSFrame(GuiAppContext context) {
        super(context, "In-Store POS", "In-Store POS", null, null);
    }

    @Override
    protected Bill.SaleType getSaleType() {
        return Bill.SaleType.IN_STORE;
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