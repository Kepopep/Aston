import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(String[] args) {
        try (var context = new ClassPathXmlApplicationContext(
                "applicationContext.xml")) {

            ConsoleUI consoleUI = context.getBean(ConsoleUI.class);
            UserService userService = context.getBean(UserService.class);
            consoleUI.run(userService);
        }
    }
}