package com.springsecurityjwt3.app;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
// This tells Spring to look for @Service, @Component, @Configuration in both modules
@ComponentScan(basePackages = "com.springsecurityjwt3") 
// This tells Spring where to find your Entities in core-lib
@EntityScan(basePackages = "com.springsecurityjwt3.core.entity")
// This tells Spring where to find your Repositories in core-lib
@EnableJpaRepositories(basePackages = "com.springsecurityjwt3.core.repository")
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

		// SpringApplication.run(SpringSecurityJwt3Application.class, args);
		// System.out.println("\n\nPort: 8443\n\nApplication Running Successfully.\n\n");
		// System.out.println("\n\nPort: 8080\n\nApplication Running Successfully.\n\n");


		     // Run the application and capture the context
        ConfigurableEnvironment env = SpringApplication.run(SpringSecurityJwt3Application.class, args).getEnvironment();
		   // Get the actual port from the environment
        String port = env.getProperty("server.port", "8080"); // Default to 8080 if not set
        
        // Print the actual port
        System.out.println("\n\nPort: " + port + "\n\nApplication Running Successfully.\n\n");
		
	}

}
