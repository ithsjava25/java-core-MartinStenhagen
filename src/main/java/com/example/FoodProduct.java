package com.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class FoodProduct extends Product implements Perishable,Shippable {

    private final LocalDate expirationDate;
    private final BigDecimal weight;

    public FoodProduct(
            UUID uuid,
            String name,
            Category category,
            BigDecimal price,
            LocalDate expirationDate,
            BigDecimal weight) {

        super(uuid,name, category, price);
        this.expirationDate = expirationDate;
        this.weight = weight;
    }
    public LocalDate expirationDate() {
        return expirationDate;
    }

    @Override
    public boolean isExpired() {
        return Perishable.super.isExpired();
    }

    public double weight() {
        return weight != null ? weight.doubleValue() : 0.0;
    }

    @Override
    public BigDecimal calculateShippingCost() {
        return BigDecimal.TEN;
    }

    @Override
    public String productDetails() {
        return "Food: " + name() + ", Expires: " + expirationDate + ", Weight: " + weight;
    }
}
