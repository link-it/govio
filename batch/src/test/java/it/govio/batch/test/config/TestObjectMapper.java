package it.govio.batch.test.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@TestConfiguration
public class TestObjectMapper {

	Logger log = LoggerFactory.getLogger(TestObjectMapper.class);
	
//	@Bean
//	public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
//		var ptv = BasicPolymorphicTypeValidator
//	    .builder()
//	    .allowIfBaseType(Object.class)
//	    .build();
//		
//		log.info("CONFIGURO OBJECT MAPPER");
//		return builder ->  builder.
//				defaultTyping(DefaultTypeResolverBuilder.construct(DefaultTyping.NON_FINAL, ptv)).
//				featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
//
//	}
	
	/*@Bean
	@Primary
	public ObjectMapper objectMapper() {
		log.info("CONFIGURO OBJECT MAPPER");

	    return new ObjectMapper()
	      .setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}*/
	@Autowired
	private ObjectMapper objectMapper;

	/*@Bean
	public ExecutionContextSerializer executionContextSerializer() {
		log.info("CONFIGURO OBJECT MAPPER");

		var ptv = BasicPolymorphicTypeValidator
			    .builder()
			    .allowIfBaseType(Object.class)
			    .build();

		     objectMapper.activateDefaultTyping(ptv);
		     Jackson2ExecutionContextStringSerializer serializer = new Jackson2ExecutionContextStringSerializer();
		    	     serializer.setObjectMapper(objectMapper);
		    	     // register serializer in JobRepositoryFactoryBean
		    	     
    	     return serializer;
	}*/
}
