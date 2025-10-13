package com.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class FoodProduct extends Product implements Perishable,Shippable {

    private final LocalDate expirationDate;
    private final double weight;

    public FoodProduct(
            UUID uuid,
            String name,
            Category category,
            BigDecimal price,
            LocalDate expirationDate,
            double weight) {

        super(uuid,name, category, price);
        this.expirationDate = expirationDate;
        this.weight = weight;
    }
    public LocalDate expirationDate() {
        return expirationDate;
    }
    public double weight() {
        return weight;
    }

    @Override
    public BigDecimal calculateShippingCost() {
        return null;
    }
}
