package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Warehouse {

    private static final Map<String, Warehouse> warehouseMap = new HashMap<String, Warehouse>();
    private String name;
    private Warehouse (String name){
        this.name = name;
    }
    public static Warehouse getInstance(String name) {
        return warehouseMap.get(name);

    }


    public List<Product> getProducts(){
        return new ArrayList<>();
    }

    public List<Shippable> shippableProducts() {
        return new ArrayList<>();
    }

    public void clearProducts() {
        warehouseMap.clear();
    }

    public boolean isEmpty() {
        return warehouseMap.isEmpty();
    }

    public void addProduct(Product product) {
        if(product == null){
            throw new IllegalArgumentException("Product cannot be null");
        }


    }
}
