package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleUI.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private UserService userService;
    private final Scanner scanner = new Scanner(System.in);
    private boolean running = true;

    public void run(UserService userService) {
        this.userService = userService;
        System.out.println("=== Управление пользователями ===");
        while (running) {
            printMenu();
            try {
                handleCommand(readLine("Выберите пункт: ").trim());
            } catch (InputClosedException e) {
                running = false;
            }
        }
        System.out.println("До свидания!");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти пользователя по id");
        System.out.println("3. Показать всех пользователей");
        System.out.println("4. Обновить пользователя");
        System.out.println("5. Удалить пользователя");
        System.out.println("0. Выход");
    }

    private void handleCommand(String command) {
        try {
            switch (command) {
                case "1" -> createUser();
                case "2" -> showUser();
                case "3" -> showAllUsers();
                case "4" -> updateUser();
                case "5" -> deleteUser();
                case "0" -> running = false;
                default -> System.out.println("Неизвестная команда, введите число от 0 до 5.");
            }
        } catch (InputClosedException e) {
            throw e;
        } catch (IllegalArgumentException | UserNotFoundException | DaoException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Непредвиденная ошибка при выполнении команды {}", command, e);
            System.out.println("Непредвиденная ошибка. Подробности в logs/app.log");
        }
    }

    // ===== Команды =====

    private void createUser() {
        String name = readLine("Имя: ");
        String email = readLine("Email: ");
        Integer age = readOptionalInteger("Возраст (Enter — пропустить): ");

        User user = userService.save(name, email, age);
        System.out.println("Пользователь создан:");
        printUser(user);
    }

    private void showUser() {
        Long id = readLong("Введите id: ");
        printUser(userService.findById(id));
    }

    private void showAllUsers() {
        List<User> users = userService.findAll();
        if (users.isEmpty()) {
            System.out.println("Пользователей пока нет.");
            return;
        }

        String format = "%-6s %-20s %-30s %-8s %-20s%n";
        System.out.printf(format, "ID", "Имя", "Email", "Возраст", "Создан");
        System.out.println("-".repeat(88));
        for (User u : users) {
            System.out.printf(format,
                    u.getId(),
                    u.getName(),
                    u.getEmail(),
                    u.getAge() == null ? "—" : u.getAge(),
                    formatDate(u));
        }
        System.out.println("Всего: " + users.size());
    }

    private void updateUser() {
        Long id = readLong("Введите id пользователя: ");
        User current = userService.findById(id);

        System.out.println("Оставьте поле пустым, чтобы не менять его.");
        String name = readLine("Имя [" + current.getName() + "]: ");
        String email = readLine("Email [" + current.getEmail() + "]: ");
        Integer age = readOptionalInteger("Возраст [" + (current.getAge() == null ? "—" : current.getAge()) + "]: ");

        if (name.isBlank() && email.isBlank() && age == null) {
            System.out.println("Изменений нет.");
            return;
        }

        User updated = userService.update(id, name, email, age);
        System.out.println("Пользователь обновлён:");
        printUser(updated);
    }

    private void deleteUser() {
        Long id = readLong("Введите id пользователя: ");
        User user = userService.findById(id);
        printUser(user);

        if (!readYesNo("Удалить этого пользователя? (y/n): ")) {
            System.out.println("Удаление отменено.");
            return;
        }

        userService.delete(id);
        System.out.println("Пользователь удалён.");
    }

    private void printUser(User u) {
        System.out.println("  ID:      " + u.getId());
        System.out.println("  Имя:     " + u.getName());
        System.out.println("  Email:   " + u.getEmail());
        System.out.println("  Возраст: " + (u.getAge() == null ? "не указан" : u.getAge()));
        System.out.println("  Создан:  " + formatDate(u));
    }

    private String formatDate(User u) {
        return u.getCreatedAt() == null ? "—" : u.getCreatedAt().format(DATE_FORMAT);
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        return scanner.nextLine();
    }

    private Long readLong(String prompt) {
        while (true) {
            String input = readLine(prompt).trim();
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Нужно целое число, попробуйте ещё раз.");
            }
        }
    }

    private Integer readOptionalInteger(String prompt) {
        while (true) {
            String input = readLine(prompt).trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Нужно целое число или пустая строка, попробуйте ещё раз.");
            }
        }
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            String input = readLine(prompt).trim().toLowerCase();
            switch (input) {
                case "y", "yes", "д", "да" -> { return true; }
                case "n", "no", "н", "нет" -> { return false; }
                default -> System.out.println("Введите y или n.");
            }
        }
    }

    private static class InputClosedException extends RuntimeException {
    }
}