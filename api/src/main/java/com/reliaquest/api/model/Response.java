package com.reliaquest.api.model;

public record Response<T>(String status, T data, String message) {}
