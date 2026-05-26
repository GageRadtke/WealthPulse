package com.example.demo.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// ignore any extra data in the JSON that we don't need
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoldApiResponse {
    public double price;
}