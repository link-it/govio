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
import it.govhub.govio.api.assemblers.FileMessageAssembler;
import it.govhub.govio.api.assemblers.MessageAssembler;
import it.govhub.govio.api.beans.FileList;
import it.govhub.govio.api.beans.FileMessageList;
import it.govhub.govio.api.beans.GovioFile;
import it.govhub.govio.api.config.GovioRoles;
import it.govhub.govio.api.entity.GovioFileEntity;
import it.govhub.govio.api.entity.GovioFileMessageEntity;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.messages.FileMessages;
import it.govhub.govio.api.repository.GovioFileMessageRepository;
import it.govhub.govio.api.repository.GovioFileRepository;
import it.govhub.govio.api.repository.GovioMessageRepository;
import it.govhub.govio.api.repository.GovioServiceInstanceRepository;
import it.govhub.govregistry.commons.exception.InternalException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.security.services.SecurityService;

@Service
public class FileService {

	@Value("${govio.filerepository.path:/var/govio/csv}")
	Path fileRepositoryPath;
	
	@Autowired
	GovioFileRepository fileRepo;

	@Autowired
	GovioServiceInstanceRepository serviceRepo;
	
	@Autowired
	SecurityService authService;
	
	@Autowired
	FileAssembler fileAssembler;
	
	@Autowired
	GovioFileMessageRepository fileMessageRepo;
	
	@Autowired
	FileMessageAssembler fileMessageAssembler;
	
	@Autowired
	GovioMessageRepository messageRepo;
	
	@Autowired
	MessageAssembler messageAssembler;
	
	@Autowired
	FileMessages fileMessages;
	
	Logger logger = LoggerFactory.getLogger(FileService.class);
	
	@Transactional
	public GovioFileEntity uploadCSV(GovioServiceInstanceEntity instance, String sourceFilename, FileItemStream itemStream) {
		
		this.authService.hasAnyOrganizationAuthority(instance.getOrganization().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_SYSADMIN);
		this.authService.hasAnyServiceAuthority(instance.getService().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_SYSADMIN) ;
		
		if (this.fileRepo.findByNameAndServiceInstance(sourceFilename, instance).isPresent()) {
			throw new SemanticValidationException(this.fileMessages.conflict("name", sourceFilename));
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
	public GovioFile readFile(Long id) {
		
		GovioFileEntity file = this.fileRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.fileMessages.idNotFound(id)));
		GovioServiceInstanceEntity instance = file.getServiceInstance();
		
		this.authService.hasAnyOrganizationAuthority(instance.getOrganization().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER, GovioRoles.GOVIO_SYSADMIN);
		this.authService.hasAnyServiceAuthority(instance.getService().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER, GovioRoles.GOVIO_SYSADMIN) ;
		
		GovioFile ret = this.fileAssembler.toModel(file);
		
		return ret;
	}

	
	@Transactional
	public FileList listFiles(Specification<GovioFileEntity> spec, LimitOffsetPageRequest pageRequest) {
		Page<GovioFileEntity> files= this.fileRepo.findAll(spec, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		FileList ret = ListaUtils.buildPaginatedList(files, pageRequest.limit, curRequest, new FileList());
		
		for (GovioFileEntity file : files) {
			ret.addItemsItem(this.fileAssembler.toModel(file));
		}
		return ret;
	}
	
	
	@Transactional
	public FileMessageList listFileMessages(Specification<GovioFileMessageEntity> spec, LimitOffsetPageRequest pageRequest) {
		
		// TODO: Qui ho bisogno di un'entity graph che di ogni fileEntity mi peschi anche i
		// fileMessages, altrimenti pago altre
		// N query quando vado a convertire i files
		
		Page<GovioFileMessageEntity> fileMessages = this.fileMessageRepo.findAll(spec, pageRequest.pageable);

		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();

		FileMessageList ret = ListaUtils.buildPaginatedList(fileMessages, pageRequest.limit, curRequest,
				new FileMessageList());

		for (GovioFileMessageEntity fileMessage : fileMessages) {
			ret.addItemsItem(this.fileMessageAssembler.toModel(fileMessage));
		}
		
		return ret;
	}
	
	
}
