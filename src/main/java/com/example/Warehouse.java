package com.example;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class Warehouse {

    private static final Map<String, Warehouse> warehouseMap = new HashMap<>();

    private final String name;

    private final List<Product> products = new ArrayList<>();

    private final Set<UUID> changedProducts = new HashSet<>();

    private Warehouse(String name) {
        this.name = name;
    }


    public static Warehouse getInstance(String name) {
        return warehouseMap.computeIfAbsent(name, Warehouse::new);
    }

    public static Warehouse getInstance() {
        return getInstance("default");
    }


    public List<Product> getProducts() {
        return List.copyOf(products);
    }

    public List<Shippable> shippableProducts() {
        List<Shippable> shippables = new ArrayList<>();
        for (Product product : products) {
            if (product instanceof Shippable) {
                shippables.add((Shippable) product);
            }
        }
        return shippables;
    }

    public void clearProducts() {
        products.clear();
    }

    public boolean isEmpty() {
        return products.isEmpty();
    }

    public String getName() {
        return name;
    }

    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        boolean alreadyExists = products.stream()
                .anyMatch(p -> p.uuid().equals(product.uuid()));

        if (alreadyExists) {
            throw new IllegalArgumentException("Product with that id already exists, use updateProduct for updates.");
        }


        products.add(product);
    }

    public Optional<Product> getProductById(UUID id) {
        for (Product product : products) {
            if (product.uuid().equals(id)) {
                return Optional.of(product);
            }
        }
        return Optional.empty();
    }

    public void remove(UUID uuid) {
        //metod som anropar annan medtod uuid() i den abstrakta klassen.
        products.removeIf(product -> product.uuid().equals(uuid));
    }

    public void updateProductPrice(UUID uuid, BigDecimal newPrice) {
        Product product = products.stream()
                .filter(p -> p.uuid().equals(uuid))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + uuid));

        product.setPrice(newPrice);
        changedProducts.add(uuid);
    }


    public List<Product> getChangedProducts() {
        List<Product> changedList = new ArrayList<>();
        for (Product product : products) {
            if (changedProducts.contains(product.uuid())) {
                changedList.add(product);
            }
        }
        return changedList;
    }

    public List<Perishable> expiredProducts() {
        return products.stream()
                .filter(p -> p instanceof Perishable)
                .map(p -> (Perishable) p)
                .filter(Perishable::isExpired)
                .collect(Collectors.toList());
    }


    public Map<Category, List<Product>> getProductsGroupedByCategories() {
        return products.stream()
                .collect(Collectors.groupingBy(Product::category, Collectors.toUnmodifiableList())); // ändrat till unmodifiable.


    }


}
