package com.looksee.visualDesignAudit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SpringBootApplication
@ComponentScan(basePackages = {"com.looksee*"})
@PropertySources({
	@PropertySource("classpath:application.properties")
})
@EnableNeo4jRepositories("com.looksee.visualDesignAudit.models.repository")
@EntityScan(basePackages = { "com.looksee.visualDesignAudit.models"} )
public class Application {
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private static final Random rand = new Random(2020);

	public static void main(String[] args)  {
		SpringApplication.run(Application.class, args);
	}

}
