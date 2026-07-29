package com.example.demo.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AlphaVantageResponse {
    @JsonProperty("Global Quote")
    private GlobalQuote globalQuote;

    public GlobalQuote getGlobalQuote() {
        return globalQuote;
    }

    public static class GlobalQuote {
        @JsonProperty("05. price")
        private String price;

        public String getPrice() {
            return price;
        }
    }
}