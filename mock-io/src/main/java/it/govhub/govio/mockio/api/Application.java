package it.govhub.govio.mockio.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.MapperFeature;

import it.govhub.govregistry.commons.exception.handlers.RequestRejectedExceptionHandler;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	

	/**
	 * Modifichiamo il serializzatore JSON in modo da serializzare le Base64String
	 * come stringhe normali
	 */
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
		return builder ->  builder.
				featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

	}

	@Configuration
	static class WebMvcConfig implements WebMvcConfigurer {
		
		Logger log = LoggerFactory.getLogger(WebMvcConfig.class);
		
		/**
		 * Questa  serve per serializzare correttamente gli enum passati via
		 * parametro query. Altrimenti è necessario passarli in upperCase.
		 *
		 */
		@Override
		public void addFormatters(FormatterRegistry registry) {
			ApplicationConversionService.configure(registry);
		}
		
		/**
		 * Ignoriamo lo header Accept, avendo un solo content-type da restituire per endpoint.
		 * Disabilitiamo di fatto la content-negotiation.
		 */
		@Override
		public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
			log.info("Configuring the content negotiator...");
		    configurer.
		    favorParameter(false).
		    ignoreAcceptHeader(true).
		    defaultContentType(MediaType.parseMediaType("application/hal+json"), MediaType.ALL );
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