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
package it.govhub.govio.api.web;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.constraints.Min;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import it.govhub.govio.api.assemblers.FileAssembler;
import it.govhub.govio.api.beans.EmbedFileEnum;
import it.govhub.govio.api.beans.FileList;
import it.govhub.govio.api.beans.FileMessageList;
import it.govhub.govio.api.beans.FileMessageStatusEnum;
import it.govhub.govio.api.beans.FileOrdering;
import it.govhub.govio.api.beans.FileStatsInner;
import it.govhub.govio.api.beans.GovioFile;
import it.govhub.govio.api.config.GovioRoles;
import it.govhub.govio.api.entity.GovioFileEntity;
import it.govhub.govio.api.entity.GovioFileMessageEntity;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.messages.FileMessages;
import it.govhub.govio.api.messages.ServiceInstanceMessages;
import it.govhub.govio.api.repository.FileFilters;
import it.govhub.govio.api.repository.FileMessageFilters;
import it.govhub.govio.api.repository.FileRepository;
import it.govhub.govio.api.repository.ServiceInstanceRepository;
import it.govhub.govio.api.services.FileService;
import it.govhub.govio.api.spec.FileApi;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.exception.BadRequestException;
import it.govhub.govregistry.commons.exception.InternalException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.commons.utils.RequestUtils;
import it.govhub.security.services.SecurityService;

@V1RestController
public class FileController implements FileApi {
	
	@Autowired
	ServiceInstanceRepository serviceRepo;
	
	@Autowired
	FileService fileService;
	
	@Autowired
	FileAssembler fileAssembler;
	
	@Autowired
	SecurityService authService;
	
	@Autowired
	FileRepository fileRepo;
	
	@Autowired
	FileMessages fileMessages;
	
	@Autowired
	ServiceInstanceMessages sinstanceMessages;
	
	Logger log = LoggerFactory.getLogger(FileController.class);
	
	@Override
	public ResponseEntity<GovioFile> uploadFile(Long serviceInstanceId,  MultipartFile file) {
		
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		
		// Leggo il body multipart ed estraggo nomeFile e stream di input
		String sourceFilename = null;
		FileItemStream itemStream = null;
		
		try {
			FileItemIterator iterStream = new ServletFileUpload().getItemIterator(request);
			
			// Leggo il body multipart ed estraggo il file,
			// NOTA: la clausola del while DEVE essere scritta in questo ordine. Lo stream corrente viene invalidato
			// quando viene chiamato iterStream.hasNext.
			// Per lo short-circuit dell'&&, una volta trovato l'elemento multipart necessario, usciamo dal while
			// senza chiamare iterStream.hasNext
			log.debug("Reading Multipart Elements..");
			while (sourceFilename == null && iterStream.hasNext()) {
			    itemStream = iterStream.next();
			    log.debug("Found element: {}", itemStream.getFieldName());
			    
			    if (itemStream.isFormField()) {
			    	log.debug("Skipping multipart form field {}", itemStream.getFieldName());
			    } else {
				    sourceFilename = RequestUtils.readFilenameFromHeaders(itemStream.getHeaders());
			    }
			}
		} catch(FileUploadException e) {
			throw new BadRequestException(e);
		}
		catch (Exception e) {
			throw new InternalException(e);
		}
		
    	if (StringUtils.isEmpty(sourceFilename)) {
    		throw new BadRequestException("E' necessario indicare il filename nello header Content-Disposition del blocco multipart del file.\ne.g: [Content-Disposition: form-data; name=\"file\"; filename=\"file.csv\"] ");
    	}
    	
    	GovioServiceInstanceEntity serviceInstance = null;
  		serviceInstance = this.serviceRepo.findById(serviceInstanceId)
       			.orElseThrow( () -> new SemanticValidationException(this.sinstanceMessages.idNotFound(serviceInstanceId)));	
    	
    	if (!serviceInstance.getEnabled() ) {
    		throw new SemanticValidationException("La service instance ["+serviceInstance.getId()+"] è disabilitata.");
    	}
		
    	GovioFileEntity created = this.fileService.uploadCSV(serviceInstance, sourceFilename, itemStream);
    	
		return ResponseEntity.ok(this.fileAssembler.toModel(created));
	}
	
	
	@Transactional
	@Override
	public ResponseEntity<FileList> listFiles(
				Direction sortDirection, 
				FileOrdering orderBy,
				Integer limit, 
				Long offset, 
				String q,
				 Long userId,
				 Long serviceId,
				 Long organizationId, 
				 OffsetDateTime creationDateFrom, 
				 OffsetDateTime creationDateTo,
				 GovioFileEntity.Status status,
				 List<EmbedFileEnum> embed) {
		
		// Pesco servizi e autorizzazioni che l'utente può leggere
		Set<Long> orgIds = this.authService.listAuthorizedOrganizations(GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER);
		Set<Long> serviceIds = this.authService.listAuthorizedServices(GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER);
		
		Specification<GovioFileEntity> spec = FileFilters.empty();
		
		// Se ho dei vincoli di lettura li metto nella spec
		if (orgIds != null) {
			spec = spec.and(FileFilters.byOrganizations(orgIds));
		}
		if (serviceIds != null) {
			spec = spec.and(FileFilters.byServices(serviceIds));
		}
		
		if (userId != null) {
			spec = spec.and(FileFilters.byUser(userId));
		}
		if (serviceId != null) {
			spec = spec.and(FileFilters.byService(serviceId));
		}
		if (organizationId != null) {
			spec = spec.and(FileFilters.byOrganization(organizationId));
		}
		if (q != null) {
			spec = spec.and(FileFilters.likeFileName(q));
		}
		if(creationDateFrom != null) {
			spec = spec.and(FileFilters.fromCreationDate(creationDateFrom));
		}
		if(creationDateTo != null) {
			spec = spec.and(FileFilters.untilCreationDate(creationDateTo));
		}
		if(status!=null) {
			spec = spec.and(FileFilters.byStatus(status));
		}
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, FileFilters.sort(sortDirection,orderBy));
		
		Page<GovioFileEntity> files= this.fileRepo.findAll(spec, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		FileList ret = ListaUtils.buildPaginatedList(files, pageRequest.limit, curRequest, new FileList());
		
		for (GovioFileEntity file : files) {
			ret.addItemsItem(this.fileAssembler.toEmbeddedModel(file, embed));
		}
		
		return ResponseEntity.ok(ret);
	}


	@Transactional
	@Override
	public ResponseEntity<GovioFile> readFile(Long id) {
		
		GovioFileEntity file = this.fileRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.fileMessages.idNotFound(id)));
		GovioServiceInstanceEntity instance = file.getServiceInstance();
		
		log.debug("Reading file [{}]", id);
		
		this.authService.hasAnyOrganizationAuthority(instance.getOrganization().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER, GovioRoles.GOVIO_SYSADMIN);
		this.authService.hasAnyServiceAuthority(instance.getService().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER, GovioRoles.GOVIO_SYSADMIN) ;
		
		GovioFile ret = this.fileAssembler.toEmbeddedModel(file);
		return ResponseEntity.ok(	ret);
	}


	@Override
	public ResponseEntity<Resource> readFileContent(Long id) {
	
		GovioFileEntity file = this.fileRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.fileMessages.idNotFound(id)));
		GovioServiceInstanceEntity instance = file.getServiceInstance();
		
		this.authService.hasAnyOrganizationAuthority(instance.getOrganization().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER, GovioRoles.GOVIO_SYSADMIN);
		this.authService.hasAnyServiceAuthority(instance.getService().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER, GovioRoles.GOVIO_SYSADMIN) ;
		
		Path path = file.getLocation();
		
		FileInputStream stream;
		try {
			stream = new FileInputStream(path.toFile());
		} catch (FileNotFoundException e) {
			throw new InternalException(e);
		}

		InputStreamResource fileStream = new InputStreamResource(stream);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentLength(file.getSize());
		return new ResponseEntity<>(fileStream, headers, HttpStatus.OK); 
	}


	@Override
	public ResponseEntity<FileMessageList> readFileMessages(
			Long id,
            FileMessageStatusEnum status,
            Integer limit,
            Long offset, 
            Long lineNumberFrom) {
		
		GovioFileEntity file = this.fileRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.fileMessages.idNotFound(id)));
		GovioServiceInstanceEntity instance = file.getServiceInstance();
		
		this.authService.hasAnyOrganizationAuthority(instance.getOrganization().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER, GovioRoles.GOVIO_SYSADMIN);
		this.authService.hasAnyServiceAuthority(instance.getService().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER, GovioRoles.GOVIO_SYSADMIN) ;

		Specification<GovioFileMessageEntity> spec = FileMessageFilters.ofFile(file.getId());

		if (lineNumberFrom != null) {
			spec = spec.and(FileMessageFilters.fromLineNumber(lineNumberFrom));
		}

		if (status == FileMessageStatusEnum.ACQUIRED) {
			spec = spec.and(FileMessageFilters.acquired());
		} else if (status == FileMessageStatusEnum.ERROR) {
			spec = spec.and(FileMessageFilters.error());
		}

		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit,
				FileMessageFilters.sortByLineNumber());

		FileMessageList ret = this.fileService.listFileMessages(spec, pageRequest);
		
		return ResponseEntity.ok(ret);
	}


	@Override
	public ResponseEntity<List<FileStatsInner>> readFileMessagesStats(@Min(0) Long id) {
		
		GovioFileEntity file = this.fileRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.fileMessages.idNotFound(id)));
		GovioServiceInstanceEntity instance = file.getServiceInstance();
		
		this.authService.hasAnyOrganizationAuthority(instance.getOrganization().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER, GovioRoles.GOVIO_SYSADMIN);
		this.authService.hasAnyServiceAuthority(instance.getService().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER, GovioRoles.GOVIO_SYSADMIN) ;
		
		return ResponseEntity.ok(this.fileService.getFileStats(id));
	}

	
}
