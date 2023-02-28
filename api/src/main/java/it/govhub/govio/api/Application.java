package it.govhub.govio.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import it.govhub.govregistry.commons.config.GovhubApplication;
import it.govhub.govregistry.readops.api.config.ReadOpsExportedBeans;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@Import({ ReadOpsExportedBeans.class})
@EnableJpaRepositories("it.govhub.govio.api.repository")
@EntityScan("it.govhub.govio.api.entity")
public class Application extends GovhubApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
}