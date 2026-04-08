package ru.mirea.recom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import ru.mirea.recom.service.UserService;

@SpringBootApplication
public class RecomApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(RecomApplication.class, args);
		// Получаем бин UserService из контекста
		//UserService userService = context.getBean(UserService.class);

		// Теперь бин инициализирован, userRepository не будет null
		//userService.register("testuser", "password123");
	}

}
