/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govhub.govio.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SpringDocConfiguration;
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
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.MapperFeature;

import it.govhub.govregistry.commons.config.CommonsExportedBeans;
import it.govhub.govregistry.commons.config.TimeZoneConfigurer;
import it.govhub.govregistry.commons.exception.handlers.RequestRejectedExceptionHandler;
import it.govhub.govregistry.commons.utils.Base64String;
import it.govhub.govregistry.commons.utils.Base64StringSerializer;
import it.govhub.govregistry.readops.api.config.ReadOpsExportedBeans;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@Import({ ReadOpsExportedBeans.class,  CommonsExportedBeans.class,   TimeZoneConfigurer.class})
@EnableJpaRepositories("it.govhub.govio.api.repository")
@EntityScan("it.govhub.govio.api.entity")
public class Application extends SpringBootServletInitializer {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Value("${govhub.time-zone:Europe/Rome}")
	String timeZone;


	@Bean
	public ForwardedHeaderFilter forwardedHeaderFilter() {
		return new ForwardedHeaderFilter();
	}

	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
		return builder ->  builder.
				timeZone(this.timeZone).
				serializerByType(Base64String.class, new Base64StringSerializer()).
				featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

	}

	@Configuration
	static class WebMvcConfig implements WebMvcConfigurer {
		
		Logger log = LoggerFactory.getLogger(WebMvcConfig.class);
		
		@Override
		public void addFormatters(FormatterRegistry registry) {
			ApplicationConversionService.configure(registry);
		}
		
		@Override
		public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
			log.info("Configuring the content negotiator...");
		    configurer.
		    favorParameter(false).
		    ignoreAcceptHeader(true).
		    defaultContentType(MediaType.parseMediaType("application/hal+json"), MediaType.ALL );
		}
	}
	
	@Bean
	public RequestRejectedHandler requestRejectedHandler() {
	   return new RequestRejectedExceptionHandler();
	}
	
	
	@Primary
	@Bean
	SpringDocConfiguration springDocConfiguration(){
	   return new SpringDocConfiguration();
	}
	@Primary
	@Bean
	SpringDocConfigProperties springDocConfigProperties() {
	   return new SpringDocConfigProperties();
	}
	
	
}