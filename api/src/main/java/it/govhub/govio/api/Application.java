package it.govhub.govio.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.format.FormatterRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import it.govhub.govregistry.commons.config.CommonsExportedBeans;
import it.govhub.govregistry.commons.config.TimeZoneConfigurer;
import it.govhub.govregistry.commons.exception.handlers.RequestRejectedExceptionHandler;
import it.govhub.govregistry.commons.utils.Base64String;
import it.govhub.govregistry.commons.utils.Base64StringSerializer;
import it.govhub.security.config.SecurityExportedBeans;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@Import({ CommonsExportedBeans.class, SecurityExportedBeans.class, TimeZoneConfigurer.class })
@EnableJpaRepositories("it.govhub.govio.api.repository")
@EntityScan("it.govhub.govio.api.entity")
public class Application extends SpringBootServletInitializer {
	
	@Value("${govhub.time-zone}")
	String timeZone;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	

	/**
	 * Gestisce la famiglia di header X-Forwarded o lo header Forwarded.
	 * Utile per la scrittura dei link hateoas in modo che tengano conto della presenza del proxy. 
	 */
	@Bean
	public ForwardedHeaderFilter forwardedHeaderFilter() {
		return new ForwardedHeaderFilter();
	}

	/**
	 * Modifichiamo il serializzatore JSON in modo da serializzare le Base64String
	 * come stringhe normali
	 */
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
		return builder ->  builder.
				timeZone(this.timeZone).
				serializerByType(Base64String.class, new Base64StringSerializer());
	}

	/**
	 * Questa  serve per serializzare correttamente gli enum passati via
	 * parametro query. Altrimenti è necessario passarli in upperCase.
	 *
	 */
	@Configuration
	static class MyConfig implements WebMvcConfigurer {
		@Override
		public void addFormatters(FormatterRegistry registry) {
			ApplicationConversionService.configure(registry);
		}
	}
	
	
	/**
	 * Questo Bean Restituisce un Problem quando spring-security rifiuta una
	 * richiesta perchè ritenuta ad esempio non sicura.
	 */
	@Bean
	public RequestRejectedHandler requestRejectedHandler() {
	   return new RequestRejectedExceptionHandler();
	}
	
	
}