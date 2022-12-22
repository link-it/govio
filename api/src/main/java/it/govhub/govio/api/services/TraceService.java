package it.govhub.govio.api.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;

import javax.transaction.Transactional;

import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.govhub.govio.api.entity.GovIOFileEntity;
import it.govhub.govio.api.entity.ServiceInstanceEntity;
import it.govhub.govio.api.repository.GovIOFileRepository;
import it.govhub.govio.api.repository.ServiceInstanceEntityRepository;
import it.govhub.govio.api.security.GovIORoles;
import it.govhub.govregistry.commons.exception.InternalException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;

@Service
public class TraceService {

	@Value("${govio.filerepository.path}")
	Path fileRepositoryPath;
	
	@Autowired
	GovIOFileRepository fileRepo;

	@Autowired
	ServiceInstanceEntityRepository serviceRepo;
	
	@Autowired
	SecurityService authService;
	
	Logger logger = LoggerFactory.getLogger(TraceService.class);
	
	@Transactional
	public GovIOFileEntity uploadCSV(ServiceInstanceEntity instance, String sourceFilename, FileItemStream itemStream) {
		
		this.authService.hasAnyOrganizationAuthority(instance.getOrganization().getId(), GovIORoles.RUOLO_GOVIO_SENDER);
		this.authService.hasAnyServiceAuthority(instance.getService().getId(), GovIORoles.RUOLO_GOVIO_SENDER);
		
		if (instance.getTemplate() == null) {
			throw new SemanticValidationException("L'istanza del servizio non ha un template di messaggio associato");
		}
		if (this.fileRepo.findByNameAndServiceInstance(sourceFilename, instance).isPresent()) {
			throw new SemanticValidationException("Un file con lo stesso nome è già presente");
		}

    	Path destPath = this.fileRepositoryPath
    				.resolve(instance.getOrganization().getId().toString())
    				.resolve(instance.getService().getId().toString());
    	
    	File destDir = destPath.toFile();
    	destDir.mkdirs();
    	
    	if (!destDir.isDirectory()) {
    		logger.error("Impossibile creare la directory per conservare i files: {}", destDir);
    		throw new RuntimeException("Non è stato possibile creare la directory per conservare i files");
    	}
    	
    	Path destFile =  destPath
    				.resolve(sourceFilename);
    	
    	logger.info("Streaming uploaded csv [{}] to [{}]", sourceFilename, destFile);
    	
    	long size;
    	try(InputStream stream=itemStream.openStream()){
			size = Files.copy(stream, destFile, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new InternalException(e);	
		} 
    	
    	GovIOFileEntity file = GovIOFileEntity.builder()
    		.creationDate(OffsetDateTime.now())
    		.govauthUser(SecurityService.getPrincipal())
    		.location(destFile)
    		.name(sourceFilename)
    		.serviceInstance(instance)
    		.status("CREATED")
    		.size(size)
    		.build();
    	
    	return this.fileRepo.save(file);
	}
	
	public GovIOFileEntity readFile(Long id) {
		
		return this.fileRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException("File di id ["+id+"] non trovato."));
	}
	
	
}
