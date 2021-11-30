package com.github.jarome.service;

/**
 * Your consumer class must implement this interface
 */
public interface PulsarListener<T> {
    void onMessage(T message);
}
