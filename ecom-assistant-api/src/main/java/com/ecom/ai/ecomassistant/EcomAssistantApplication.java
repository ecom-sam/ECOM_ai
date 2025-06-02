package com.ecom.ai.ecomassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableAspectJAutoProxy
public class EcomAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcomAssistantApplication.class, args);
	}

}
