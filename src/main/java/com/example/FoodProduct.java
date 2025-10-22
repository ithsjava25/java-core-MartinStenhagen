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

        super(uuid,name, category, validatePrice(price)); // metodanrop för att kolla att price inte är negativt.
        this.expirationDate = expirationDate;
        this.weight = weight;

        if (weight.compareTo(BigDecimal.ZERO) < 0) { //ändrat från <= efter AI-feedback.
            throw new IllegalArgumentException("Weight cannot be negative.");
        }
    }
    private static BigDecimal validatePrice(BigDecimal price) {
        if (price.compareTo(BigDecimal.ZERO) < 0) { //ändrat från <= efter AI-feedback.
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        return price;
    }

    public LocalDate expirationDate() {
        return expirationDate;
    }

    @Override
    public boolean isExpired() {
        return Perishable.super.isExpired();
    }

    @Override
    public double weight() {
        return weight.doubleValue();  // konverterar BigDecimal -> double
    }

    @Override
    public BigDecimal calculateShippingCost() {
        return weight.multiply(BigDecimal.valueOf(50)); // exakt beräkning
    }


    @Override
    public String productDetails() {
        return "Food: " + name() + ", Expires: " + expirationDate;
    }
}
