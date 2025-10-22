package com.example;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzer class that provides advanced warehouse operations.
 * Students must implement these methods for the advanced tests to pass.
 */
class WarehouseAnalyzer {
    private final Warehouse warehouse;


    public WarehouseAnalyzer(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    // Search and Filter Methods

    /**
     * Finds all products whose price is within the inclusive range [minPrice, maxPrice].
     * Based on tests: products priced exactly at the boundaries must be included; values outside are excluded.
     *
     * @param minPrice the lower bound (inclusive); must not be null
     * @param maxPrice the upper bound (inclusive); must not be null and should be >= minPrice
     * @return a list of products with minPrice <= price <= maxPrice, in the warehouse's iteration order
     */
    public List<Product> findProductsInPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        List<Product> result = new ArrayList<>();
        for (Product p : warehouse.getProducts()) {
            BigDecimal price = p.price();
            if (p.price() == null) continue;
            if (price.compareTo(minPrice) >= 0 && price.compareTo(maxPrice) <= 0) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Returns all perishable products that expire within the next {@code days} days counting from today,
     * including items that expire today, and excluding items already expired. Non-perishables are ignored.
     * Test expectation: when days = 3, items expiring Today/Tomorrow/In3Days are included; older or non-perishable are not.
     *
     * @param days number of days ahead to include (e.g., 3 includes today, 1, 2, and 3 days ahead)
     * @return list of Perishable items expiring within the window
     */
    public List<Perishable> findProductsExpiringWithinDays(int days) {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(days);
        List<Perishable> result = new ArrayList<>();
        for (Product p : warehouse.getProducts()) {
            if (p instanceof Perishable per) {
                LocalDate exp = per.expirationDate();
                if (!exp.isBefore(today) && !exp.isAfter(end)) {
                    result.add(per);
                }
            }
        }
        return result;
    }

    /**
     * Performs a case-insensitive partial name search.
     * Test expectation: searching for "milk" returns all products whose name contains that substring,
     * regardless of letter casing or presence of symbols/spaces around it.
     *
     * @param searchTerm substring to search for (case-insensitive)
     * @return list of matching products
     */
    public List<Product> searchProductsByName(String searchTerm) {
        String term = searchTerm.toLowerCase(Locale.ROOT);
        List<Product> result = new ArrayList<>();
        for (Product p : warehouse.getProducts()) {
            if (p.name().toLowerCase(Locale.ROOT).contains(term)) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Returns all products whose price is strictly greater than the given price.
     * While not asserted directly by tests, this helper is consistent with price-based filtering.
     *
     * @param price threshold (exclusive)
     * @return list of products with price > threshold
     */
    public List<Product> findProductsAbovePrice(BigDecimal price) {
        List<Product> result = new ArrayList<>();
        for (Product p : warehouse.getProducts()) {
            if (p.price().compareTo(price) > 0) {
                result.add(p);
            }
        }
        return result;
    }

    // Analytics Methods

    /**
     * Computes the average price per category using product weight as the weighting factor when available.
     * Test expectation: for FoodProduct with weights, use weighted average = sum(price*weight)/sum(weight).
     * For categories that contain only non-weighted products, a simple arithmetic mean may be used.
     * The result should round to two decimals in a way that matches the test values (e.g., 11.43 for Dairy example).
     *
     * @return a map from Category to weighted average price
     */
    public Map<Category, BigDecimal> calculateWeightedAveragePriceByCategory() {
        Map<Category, List<Product>> byCat = warehouse.getProducts().stream()
                .collect(Collectors.groupingBy(Product::category));
        Map<Category, BigDecimal> result = new HashMap<>();
        for (Map.Entry<Category, List<Product>> e : byCat.entrySet()) {
            Category cat = e.getKey();
            List<Product> items = e.getValue();
            BigDecimal weightedSum = BigDecimal.ZERO;
            BigDecimal weightSum = BigDecimal.ZERO;
            for (Product p : items) {
                if (p instanceof Shippable s) {
                    BigDecimal weight = BigDecimal.valueOf(s.weight());
                    if (weight.compareTo(BigDecimal.ZERO) > 0) {
                        weightedSum = weightedSum.add(p.price().multiply(weight));
                        weightSum = weightSum.add(weight);
                    }
                }

            }
            BigDecimal avg;
            if (weightSum.compareTo(BigDecimal.ZERO) > 0) {
                avg = weightedSum.divide(weightSum, 2, RoundingMode.HALF_UP);
            } else {
                BigDecimal sum = items.stream().map(Product::price).reduce(BigDecimal.ZERO, BigDecimal::add);
                avg = sum.divide(BigDecimal.valueOf(items.size()), 2, RoundingMode.HALF_UP);
            }
            result.put(cat, avg);
        }
        return result;
    }

    /**
     * Identifies products whose price is considered an outlier based on the interquartile range (IQR).
     * A product is considered an outlier if its price lies outside the interval
     * [Q1 - threshold × IQR, Q3 + threshold × IQR], where:
     * <ul>
     *   <li>Q1 is the 25th percentile of all product prices</li>
     *   <li>Q3 is the 75th percentile</li>
     *   <li>IQR = Q3 - Q1</li>
     * </ul>
     * The threshold defines how far from the IQR bounds a price must be to be considered an outlier.
     * <p>
     * Test expectation: with a tightly clustered group and two extreme prices, calling with threshold = 1.5
     * typically returns the two extremes.
     *
     * @param threshold multiplier applied to the IQR to define outlier bounds (e.g., 1.5)
     * @return list of products considered outliers based on price
     */
    public List<Product> findPriceOutliers(double threshold) {
        List<Product> products = warehouse.getProducts();
        if (products.isEmpty()) return List.of();

        // Extrahera priser och sortera
        List<Double> prices = products.stream()
                .map(p -> p.price().doubleValue())
                .sorted()
                .collect(Collectors.toList());

        double q1 = getPercentile(prices, 25);
        double q3 = getPercentile(prices, 75);
        double iqr = q3 - q1;

        double lowerBound = q1 - threshold * iqr;
        double upperBound = q3 + threshold * iqr;

        // Returnera produkter utanför IQR-gränser
        return products.stream()
                .filter(p -> {
                    double price = p.price().doubleValue();
                    return price < lowerBound || price > upperBound;
                })
                .collect(Collectors.toList());
    }


    private double computeMedian(List<Double> sortedList) {
        int n = sortedList.size();
        if (n % 2 == 0) {
            return (sortedList.get(n / 2 - 1) + sortedList.get(n / 2)) / 2.0;
        } else {
            return sortedList.get(n / 2);
        }
    }


    /**
     * Groups all shippable products into ShippingGroup buckets such that each group's total weight
     * does not exceed the provided maximum. The goal is to minimize the number of groups and/or total
     * shipping cost, but the exact algorithm is implementation-defined (e.g., first-fit decreasing).
     * Test expectation: for a max weight of 10.0, every group's totalWeight <= 10.0 and all items are included.
     *
     * @param maxWeightPerGroup maximum total weight per group (inclusive)
     * @return list of ShippingGroup objects covering all shippable products
     */
    public List<ShippingGroup> optimizeShippingGroups(BigDecimal maxWeightPerGroup) {
        BigDecimal maxW = maxWeightPerGroup.setScale(2,RoundingMode.HALF_UP);
        List<Shippable> items = warehouse.shippableProducts().stream()
                .sorted((a, b) -> {
                    BigDecimal wa = BigDecimal.valueOf(Objects.requireNonNullElse(a.weight(), 0.0));
                    BigDecimal wb = BigDecimal.valueOf(Objects.requireNonNullElse(b.weight(), 0.0));
                    return wb.compareTo(wa);
                })
                .toList();

        List<List<Shippable>> bins = new ArrayList<>();
        for (Shippable item : items) {
            BigDecimal itemWeight = BigDecimal.valueOf(Objects.requireNonNullElse(item.weight(), 0.0)).setScale(2, RoundingMode.HALF_UP);
            boolean placed = false;

            for (List<Shippable> bin : bins) {
                BigDecimal binWeight = bin.stream()
                        .map(s -> BigDecimal.valueOf(Objects.requireNonNullElse(s.weight(), 0.0)))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (binWeight.add(itemWeight).compareTo(maxW) <= 0) {
                    bin.add(item);
                    placed = true;
                    break;
                }
            }

            if (!placed) {
                List<Shippable> newBin = new ArrayList<>();
                newBin.add(item);
                bins.add(newBin);
            }
        }

        return bins.stream().map(ShippingGroup::new).collect(Collectors.toList());
    }

    // Business Rules Methods

    /**
     * Calculates discounted prices for perishable products based on proximity to expiration.
     * Discount rules from tests:
     * - Expires today: 50% discount (price * 0.50)
     * - Expires tomorrow: 30% discount (price * 0.70)
     * - Expires within 3 days: 15% discount (price * 0.85)
     * - Otherwise (including >3 days ahead): no discount
     * Non-perishable products should retain their original price.
     *
     * @return a map from Product to its discounted price
     */
    public Map<Product, BigDecimal> calculateExpirationBasedDiscounts() {
        Map<Product, BigDecimal> result = new HashMap<>();
        LocalDate today = LocalDate.now();
        for (Product p : warehouse.getProducts()) {
            BigDecimal discounted = p.price();
            if (p instanceof Perishable per) {
                LocalDate exp = per.expirationDate();
                long daysBetween = ChronoUnit.DAYS.between(today, exp);
                if (daysBetween == 0) {
                    discounted = p.price().multiply(new BigDecimal("0.50"));
                } else if (daysBetween == 1) {
                    discounted = p.price().multiply(new BigDecimal("0.70"));
                } else if (daysBetween == 2 || daysBetween == 3) {
                    discounted = p.price().multiply(new BigDecimal("0.85"));
                } else {
                    discounted = p.price();
                }
                discounted = discounted.setScale(2, RoundingMode.HALF_UP);
            }
            result.put(p, discounted);
        }
        return result;
    }

    /**
     * Evaluates inventory business rules and returns a summary:
     * - High-value percentage: proportion of products considered high-value (e.g., price >= some threshold).
     * The tests imply a scenario where 15 of 20 items (priced 2000) yield ~75% and should trigger a warning
     * when percentage exceeds 70%.
     * - Category diversity: count of distinct categories in the inventory. The tests expect at least 2.
     * - Convenience booleans: highValueWarning (percentage > 70%) and minimumDiversity (category count >= 2).
     * <p>
     * Note: The exact high-value threshold is implementation-defined, but the provided tests create a clear
     * separation using very expensive electronics (e.g., 2000) vs. low-priced food items (e.g., 10),
     * allowing percentage computation regardless of the chosen cutoff as long as it matches the scenario.
     *
     * @return InventoryValidation summary with computed metrics
     */
    public InventoryValidation validateInventoryConstraints() {
        List<Product> items = warehouse.getProducts();
        if (items.isEmpty()) return new InventoryValidation(0.0, 0);
        BigDecimal highValueThreshold = new BigDecimal("1000");
        long highValueCount = items.stream().filter(p -> p.price().compareTo(highValueThreshold) >= 0).count();
        double percentage = (highValueCount * 100.0) / items.size();
        int diversity = (int) items.stream().map(Product::category).distinct().count();
        return new InventoryValidation(percentage, diversity);
    }

    /**
     * Aggregates key statistics for the current warehouse inventory.
     * Test expectation for a 4-item setup:
     * - totalProducts: number of products (4)
     * - totalValue: sum of prices (1590.50)
     * - averagePrice: totalValue / totalProducts rounded to two decimals (397.63)
     * - expiredCount: number of perishable items whose expiration date is before today (1)
     * - categoryCount: number of distinct categories across all products (2)
     * - mostExpensiveProduct / cheapestProduct: extremes by price
     *
     * @return InventoryStatistics snapshot containing aggregated metrics
     */
    public InventoryStatistics getInventoryStatistics() {
        List<Product> items = warehouse.getProducts();
        int totalProducts = items.size();
        BigDecimal totalValue = items.stream().map(Product::price).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averagePrice = totalProducts == 0 ? BigDecimal.ZERO : totalValue.divide(BigDecimal.valueOf(totalProducts), 2, RoundingMode.HALF_UP);
        int expiredCount = 0;
        for (Product p : items) {
            if (p instanceof Perishable per && per.expirationDate().isBefore(LocalDate.now())) {
                expiredCount++;
            }
        }
        int categoryCount = (int) items.stream().map(Product::category).distinct().count();
        Product mostExpensive = items.stream().max(Comparator.comparing(Product::price)).orElse(null);
        Product cheapest = items.stream().min(Comparator.comparing(Product::price)).orElse(null);
        return new InventoryStatistics(totalProducts, totalValue, averagePrice, expiredCount, categoryCount, mostExpensive, cheapest);
    }

    private double getPercentile(List<Double> sortedList, double percentile) {
        if (sortedList.isEmpty()) return Double.NaN;
        double index = (percentile / 100.0) * (sortedList.size() - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        if (lower == upper) {
            return sortedList.get(lower);
        } else {
            return sortedList.get(lower) +
                    (sortedList.get(upper) - sortedList.get(lower)) * (index - lower);
        }
    }
}

/**
 * Represents a group of products for shipping
 */
class ShippingGroup {
    private final List<Shippable> products;
    private final Double totalWeight;
    private final BigDecimal totalShippingCost;

    public ShippingGroup(List<Shippable> products) {
        this.products = new ArrayList<>(products);
        this.totalWeight = products.stream()
                .map(Shippable::weight)
                .reduce(0.0, Double::sum);
        this.totalShippingCost = products.stream()
                .map(Shippable::calculateShippingCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public List<Shippable> getProducts() {
        return new ArrayList<>(products);
    }

    public Double getTotalWeight() {
        return totalWeight;
    }

    public BigDecimal getTotalShippingCost() {
        return totalShippingCost;
    }
}

/**
 * Validation result for inventory constraints
 */
class InventoryValidation {
    private final double highValuePercentage;
    private final int categoryDiversity;
    private final boolean highValueWarning;
    private final boolean minimumDiversity;

    public InventoryValidation(double highValuePercentage, int categoryDiversity) {
        this.highValuePercentage = highValuePercentage;
        this.categoryDiversity = categoryDiversity;
        this.highValueWarning = highValuePercentage > 70.0;
        this.minimumDiversity = categoryDiversity >= 2;
    }

    public double getHighValuePercentage() {
        return highValuePercentage;
    }

    public int getCategoryDiversity() {
        return categoryDiversity;
    }

    public boolean isHighValueWarning() {
        return highValueWarning;
    }

    public boolean hasMinimumDiversity() {
        return minimumDiversity;
    }
}

/**
 * Comprehensive inventory statistics
 */
class InventoryStatistics {
    private final int totalProducts;
    private final BigDecimal totalValue;
    private final BigDecimal averagePrice;
    private final int expiredCount;
    private final int categoryCount;
    private final Product mostExpensiveProduct;
    private final Product cheapestProduct;

    public InventoryStatistics(int totalProducts, BigDecimal totalValue, BigDecimal averagePrice,
                               int expiredCount, int categoryCount,
                               Product mostExpensiveProduct, Product cheapestProduct) {
        this.totalProducts = totalProducts;
        this.totalValue = totalValue;
        this.averagePrice = averagePrice;
        this.expiredCount = expiredCount;
        this.categoryCount = categoryCount;
        this.mostExpensiveProduct = mostExpensiveProduct;
        this.cheapestProduct = cheapestProduct;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public int getExpiredCount() {
        return expiredCount;
    }

    public int getCategoryCount() {
        return categoryCount;
    }

    public Product getMostExpensiveProduct() {
        return mostExpensiveProduct;
    }

    public Product getCheapestProduct() {
        return cheapestProduct;
    }
}