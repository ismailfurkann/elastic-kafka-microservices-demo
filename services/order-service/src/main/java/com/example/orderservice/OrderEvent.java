package com.example.orderservice;

public record OrderEvent(String type, String product, int price) {
}
