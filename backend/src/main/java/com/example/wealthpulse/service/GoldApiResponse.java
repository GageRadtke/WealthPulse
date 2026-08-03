package com.example.wealthpulse.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoldApiResponse {
    public double price;
}
