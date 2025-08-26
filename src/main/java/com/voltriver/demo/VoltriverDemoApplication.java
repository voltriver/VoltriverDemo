package com.voltriver.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class VoltriverDemoApplication {

	public static void main(String[] args) {
		SpringApplication applicatiton = new SpringApplication(VoltriverDemoApplication.class);
		//applicatiton.setWebApplicationType(WebApplicationType.NONE);
		applicatiton.run(args);		
	}

}
