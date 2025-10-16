package com.example;

import java.util.HashMap;
import java.util.Map;

public final class Category {
    private final String name;
    private static final Map<String, Category> cache = new HashMap<>();

    private Category(String name) {
        this.name = name;
    }
    public static Category of(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Category name can't be null");
        } else if (name.isBlank()) {
            throw new IllegalArgumentException("Category name can't be blank");
        }
        String formattedName = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        return cache.computeIfAbsent(formattedName, Category::new);
    }
    public String getName() {
        return name;
    }




}
