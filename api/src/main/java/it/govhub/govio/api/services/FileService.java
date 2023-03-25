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
import it.govhub.govio.api.beans.FileMessageList;
import it.govhub.govio.api.config.GovioRoles;
import it.govhub.govio.api.entity.GovioFileEntity;
import it.govhub.govio.api.entity.GovioFileMessageEntity;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.messages.FileMessages;
import it.govhub.govio.api.repository.FileMessageRepository;
import it.govhub.govio.api.repository.FileRepository;
import it.govhub.govio.api.repository.MessageRepository;
import it.govhub.govio.api.repository.ServiceInstanceRepository;
import it.govhub.govregistry.commons.exception.InternalException;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.security.services.SecurityService;

@Service
public class FileService {

	@Value("${govio.filerepository.path:/var/govio/csv}")
	Path fileRepositoryPath;
	
	@Autowired
	FileRepository fileRepo;

	@Autowired
	ServiceInstanceRepository serviceRepo;
	
	@Autowired
	SecurityService authService;
	
	@Autowired
	FileAssembler fileAssembler;
	
	@Autowired
	FileMessageRepository fileMessageRepo;
	
	@Autowired
	FileMessageAssembler fileMessageAssembler;
	
	@Autowired
	MessageRepository messageRepo;
	
	@Autowired
	MessageAssembler messageAssembler;
	
	@Autowired
	FileMessages fileMessages;
	
	Logger log = LoggerFactory.getLogger(FileService.class);
	
	
	@Transactional
	public GovioFileEntity uploadCSV(GovioServiceInstanceEntity instance, String sourceFilename, FileItemStream itemStream) {
		log.info("Uploading file {} to Service Instance {}", sourceFilename, instance.getId());
		
		this.authService.hasAnyOrganizationAuthority(instance.getOrganization().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_SYSADMIN);
		this.authService.hasAnyServiceAuthority(instance.getService().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_SYSADMIN) ;
		
		if (this.fileRepo.findByNameAndServiceInstance(sourceFilename, instance).isPresent()) {
			throw new SemanticValidationException(this.fileMessages.conflict("name", sourceFilename));
		}

    	Path destPath = this.fileRepositoryPath
    				.resolve(instance.getId().toString());
    	
    	File destDir = destPath.toFile();
    	destDir.mkdirs();
    	
    	if (!destDir.isDirectory()) {
    		log.error("Impossibile creare la directory per conservare i files: {}", destDir);
    		throw new InternalException("Non è stato possibile creare la directory per conservare i files");
    	}
    	
    	Path destFile =  destPath
    				.resolve(sourceFilename);
    	
    	log.info("Streaming uploaded csv [{}] to [{}]", sourceFilename, destFile);
    	
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
	public FileMessageList listFileMessages(Specification<GovioFileMessageEntity> spec, LimitOffsetPageRequest pageRequest) {
		
		// TODO: Qui ho bisogno di un'entity graph che di ogni fileEntity mi peschi anche i
		// fileMessages, altrimenti pago altre
		// N query quando vado a convertire i files
		
		Page<GovioFileMessageEntity> fileList = this.fileMessageRepo.findAll(spec, pageRequest.pageable);

		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();

		FileMessageList ret = ListaUtils.buildPaginatedList(fileList, pageRequest.limit, curRequest,
				new FileMessageList());

		for (GovioFileMessageEntity fileMessage : fileList) {
			ret.addItemsItem(this.fileMessageAssembler.toModel(fileMessage));
		}
		
		return ret;
	}
	
	
}
