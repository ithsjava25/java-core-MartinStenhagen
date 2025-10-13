package com.example;

import java.math.BigDecimal;

public interface Shippable{
    double weight();
    BigDecimal calculateShippingCost();
}
