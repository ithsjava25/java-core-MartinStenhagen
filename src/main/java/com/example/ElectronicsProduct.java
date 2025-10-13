package com.example;

import java.math.BigDecimal;
import java.util.UUID;

public class ElectronicsProduct extends  Product implements Shippable {

    private int warrantyMonths;
    private BigDecimal weight;

    public ElectronicsProduct(UUID uuid, String name, Category category, BigDecimal price, int warrantyMonths, BigDecimal weight) {
        super(uuid, name, category, price);
        this.warrantyMonths = warrantyMonths;
        this.weight = weight;
    }

    @Override
    public double weight() {
        return weight.doubleValue();
    }

    @Override
    public BigDecimal calculateShippingCost() {
        return null;
    }
}
