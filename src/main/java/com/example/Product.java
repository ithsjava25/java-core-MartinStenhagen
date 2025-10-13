package com.example;

import java.math.BigDecimal;
import java.util.UUID;

public abstract class Product {
    private UUID uuid;
    private String name;
    private Category category;
    private BigDecimal price;


    public Product(UUID uuid,String name, Category category, BigDecimal price) {
        this.uuid = uuid;
        this.name = name;
        this.category = category;
        this.price = price;

    }

    public String name() {
        return name;
    }

    public Category category() {
        return category;
    }

    public UUID uuid() {
        return uuid;
    }

    public BigDecimal price() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }




}
