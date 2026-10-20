package com.example;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) { super("Не найдет пользователь id: " + id); }
}
