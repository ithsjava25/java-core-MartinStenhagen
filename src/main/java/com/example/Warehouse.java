package com.example;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class Warehouse {

    // Map för att hålla singleton-instanser per namn
    private static final Map<String, Warehouse> warehouseMap = new HashMap<>();

    private final String name;

    // Instansvariabel för att lagra produkter i detta Warehouse
    private final List<Product> products = new ArrayList<>();

    private final Set<UUID> changedProducts = new HashSet<>();

    // Privat konstruktor så att nya Warehouse skapas endast via getInstance
    private Warehouse(String name) {
        this.name = name;
    }

    // Returnerar Warehouse-instans för namnet, skapar ny om saknas
    public static Warehouse getInstance(String name) {
        return warehouseMap.computeIfAbsent(name, Warehouse::new);
    }

    // Returnerar kopia av produkterna i detta Warehouse
    public List<Product> getProducts() {
        return new ArrayList<>(products);
    }

    // Returnerar alla produkter som implementerar Shippable
    public List<Shippable> shippableProducts() {
        List<Shippable> shippables = new ArrayList<>();
        for (Product product : products) {
            if (product instanceof Shippable) {
                shippables.add((Shippable) product);
            }
        }
        return shippables;
    }

    // Rensar produkterna i detta Warehouse
    public void clearProducts() {
        products.clear();
    }

    // Returnerar true om lagret är tomt
    public boolean isEmpty() {
        return products.isEmpty();
    }

    // Returnerar Warehouse-namnet
    public String getName() {
        return name;
    }

    // Lägger till en produkt, kastar om produkten är null
    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        products.add(product);
    }

    public Optional<Product> getProductById(UUID id) {
        for (Product product : products) {
            if(product.uuid().equals(id)) {
                return Optional.of(product);
            }
        }
        return Optional.empty();
    }
// Alternativ metod med javastreams:
//   public Optional<Product> getProductById(UUID id) {
//        return products.stream()
//                .filter(p -> p.uuid().equals(id))
//                .findFirst();
//    }


    public void remove(UUID uuid) {
        //metod som anropar annan medtod uuid() i den abstrakta klassen.
        products.removeIf(product -> product.uuid().equals(uuid));
    }

    public void updateProductPrice(UUID uuid, BigDecimal newPrice) {
        Product product = products.stream()
                .filter(p -> p.uuid().equals(uuid))
                .findFirst()
                .orElseThrow(()-> new NoSuchElementException("Product not found with id:"));


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
                .collect(Collectors.groupingBy(Product::category));
    }

}
