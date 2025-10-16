package com.example;

import java.math.BigDecimal;
import java.util.UUID;

public class ElectronicsProduct extends  Product implements Shippable {

    private int warrantyMonths;
    private BigDecimal weight;

    public ElectronicsProduct(UUID uuid, String name, Category category, BigDecimal price, int warrantyMonths, BigDecimal weight) {
        super(uuid, name, category, price);
        if(warrantyMonths < 0){
            throw new IllegalArgumentException("Warranty months cannot be negative.");
        }
        this.warrantyMonths = warrantyMonths;
        this.weight = weight;
    }

    @Override
    public double weight() {
        return  weight != null ? weight.doubleValue() : 0.0;
    }

    @Override
    public BigDecimal calculateShippingCost() {
        return null;
    }

    @Override
    public String productDetails() {
        return "Electronics: " + name() + " " + category() + " " + price() + "Warranty: " + warrantyMonths + " months.";
    }
}
