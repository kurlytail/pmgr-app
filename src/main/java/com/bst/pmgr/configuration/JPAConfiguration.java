package com.bst.pmgr.configuration;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JPAConfiguration {
	
	@Bean
	public EntityManagerFactory createEntityManagerFactory() {
		return Persistence.createEntityManagerFactory("h2");
	}
}
