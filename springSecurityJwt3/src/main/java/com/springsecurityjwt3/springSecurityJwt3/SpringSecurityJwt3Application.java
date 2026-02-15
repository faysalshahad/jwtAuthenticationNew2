package com.springsecurityjwt3.springSecurityJwt3;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringSecurityJwt3Application {

	public static void main(String[] args) {

		// Get the current working directory to see where we are looking
//		String rootPath = System.getProperty("user.dir");
//		System.out.println("Looking for .env in: " + rootPath);

		//Making sure that system or main application reads .env files
		// Load .env file
		Dotenv dotenv = Dotenv.configure()
				.directory("./springSecurityJwt3") // Explicitly point to the child directory because this project has a parent folder named springSecurityJwt3 and path the that folder is "./" but the child folder path has been defined here
				.ignoreIfMissing().
				load();

		// Manually set them as System Properties so Spring @Value can see them
		dotenv.entries().forEach(dotenvEntry -> System.setProperty(dotenvEntry.getKey(),dotenvEntry.getValue()));

		SpringApplication.run(SpringSecurityJwt3Application.class, args);
		System.out.println("\n\nPort: 8443\n\nApplication Running Successfully.\n\n");
	}

}
