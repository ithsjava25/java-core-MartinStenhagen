package com.example;

import java.math.BigDecimal;
import java.util.UUID;

public class ElectronicsProduct extends  Product implements Shippable {

    private final int warrantyMonths;
    private final BigDecimal weight;

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
        return weight.doubleValue();  // konvertering krävs enligt interface
    }

    @Override
    public BigDecimal calculateShippingCost() {
        BigDecimal base = BigDecimal.valueOf(79);
        BigDecimal extra = BigDecimal.valueOf(49);

        if (weight.compareTo(BigDecimal.valueOf(5.0)) > 0) {
            return base.add(extra);
        }
        return base;
    }


    @Override
    public String productDetails() {
        return "Electronics: " + name() +", " + "Warranty: " + warrantyMonths + " months";
    }
}
