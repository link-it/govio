package it.govhub.govio.api.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govhub.govio.api.assemblers.FileAssembler;
import it.govhub.govio.api.beans.FileList;
import it.govhub.govio.api.beans.GovIOFile;
import it.govhub.govio.api.entity.GovioFileEntity;
import it.govhub.govio.api.entity.ServiceInstanceEntity;
import it.govhub.govio.api.repository.GovioFileRepository;
import it.govhub.govio.api.repository.ServiceInstanceEntityRepository;
import it.govhub.govio.api.security.GovIORoles;
import it.govhub.govio.api.web.TraceController;
import it.govhub.govregistry.commons.exception.InternalException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.security.services.SecurityService;

@Service
public class TraceService {

	@Value("${govio.filerepository.path}")
	Path fileRepositoryPath;
	
	@Autowired
	GovioFileRepository fileRepo;

	@Autowired
	ServiceInstanceEntityRepository serviceRepo;
	
	@Autowired
	SecurityService authService;
	
	@Autowired
	FileAssembler fileAssembler;
	
	Logger logger = LoggerFactory.getLogger(TraceService.class);
	
	@Transactional
	public GovioFileEntity uploadCSV(ServiceInstanceEntity instance, String sourceFilename, FileItemStream itemStream) {
		
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
    	
    	GovioFileEntity file = GovioFileEntity.builder()
    		.creationDate(OffsetDateTime.now())
    		.govauthUser(SecurityService.getPrincipal())
    		.location(destFile)
    		.name(sourceFilename)
    		.serviceInstance(instance)
    		.status(GovioFileEntity.Status.CREATED)
    		.size(size)
    		.build();
    	
    	return this.fileRepo.save(file);
	}
	

	@Transactional
	public GovIOFile readFile(Long id) {
		return this.fileRepo.findById(id)
				.map( f -> this.fileAssembler.toModel(f))
				.orElseThrow( () -> new ResourceNotFoundException("File di id ["+id+"] non trovato."));
	}

	
	@Transactional
	public FileList listFiles(Specification<GovioFileEntity> spec, LimitOffsetPageRequest pageRequest) {
		Page<GovioFileEntity> files= this.fileRepo.findAll(spec, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		FileList ret = ListaUtils.costruisciListaPaginata(files, pageRequest.limit, curRequest, new FileList());
		
		for (GovioFileEntity file : files) {
			ret.addItemsItem(this.fileAssembler.toModel(file));
		}
		return ret;
	}
	
	
}
