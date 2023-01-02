package it.govhub.govio.api.web;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.time.OffsetDateTime;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileItemHeaders;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import it.govhub.govio.api.assemblers.FileAssembler;
import it.govhub.govio.api.beans.FileList;
import it.govhub.govio.api.beans.FileMessageList;
import it.govhub.govio.api.beans.GovIOFile;
import it.govhub.govio.api.entity.GovioFileEntity;
import it.govhub.govio.api.entity.ServiceInstanceEntity;
import it.govhub.govio.api.repository.GovioFileFilters;
import it.govhub.govio.api.repository.GovioFileRepository;
import it.govhub.govio.api.repository.ServiceInstanceEntityRepository;
import it.govhub.govio.api.security.GovIORoles;
import it.govhub.govio.api.services.TraceService;
import it.govhub.govio.api.spec.TraceApi;
import it.govhub.govregistry.commons.exception.BadRequestException;
import it.govhub.govregistry.commons.exception.InternalException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;

@RestController
public class TraceController implements TraceApi {
	
	@Autowired
	ServiceInstanceEntityRepository serviceRepo;
	
	@Autowired
	TraceService traceService;
	
	@Autowired
	FileAssembler fileAssembler;
	
	@Autowired
	SecurityService authService;
	
	@Autowired
	GovioFileRepository fileRepo;
	
	Logger logger = LoggerFactory.getLogger(TraceController.class);
	
	/**
	 * I parametri argomento vengono ignorati e sono null. Abbiamo disabilitato la gestione del multipart di spring 
	 * in modo da poter utilizzare commons-fileupload di apache in modo da fare lo streaming della
	 * richiesta direttamente su file.
	 */
	@Override
	public ResponseEntity<GovIOFile> uploadCsvTrace(Long serviceId, Long organizationId, MultipartFile file) {
		
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
			
			while (sourceFilename == null && iterStream.hasNext()) {
			    itemStream = iterStream.next();
			    
			    if (itemStream.isFormField()) {
			    	logger.debug("Skipping multipart form field {}", itemStream.getFieldName());
			    } else {
				    sourceFilename = readFilenameFromHeaders(itemStream.getHeaders());
			    }
			}
		} catch (Exception e) {
			throw new InternalException(e);
		}
		
    	if (StringUtils.isEmpty(sourceFilename)) {
    		throw new BadRequestException("E' necessario indicare il filename nello header Content-Disposition del blocco multipart del file.\ne.g: [Content-Disposition: form-data; name=\"file\"; filename=\"file.csv\"] ");
    	}
    	
    	ServiceInstanceEntity serviceInstance = this.serviceRepo.findByService_IdAndOrganization_Id(serviceId, organizationId)
    			.orElseThrow( () -> new SemanticValidationException("L'istanza di servizio indicata non esiste"));
		
    	GovioFileEntity created = this.traceService.uploadCSV(serviceInstance, sourceFilename, itemStream);
    	
		return ResponseEntity.ok(this.fileAssembler.toModel(created));
	}
	
	
	private String readFilenameFromHeaders(FileItemHeaders headers) {
		
    	String filename = null;
    	try {
	    	String contentDisposition = headers.getHeader("Content-Disposition");
	    	logger.debug("Content Disposition Header: {}", contentDisposition);
	    	
	    	String[] headerDirectives = contentDisposition.split(";");
	    	
	    	for(String directive : headerDirectives) {
	    		String[] keyValue = directive.split("=");
	    		if (StringUtils.equalsIgnoreCase(keyValue[0].trim(), "filename")) {
	    			// Rimuovo i doppi apici
	    			filename = keyValue[1].trim().substring(1, keyValue[1].length()-1);
	    		}
	    	}
    	} catch (Exception e) {
    		filename = null;
    	}
    	
    	return filename;
	}


	@Override
	public ResponseEntity<FileList> listCsvTrace(
				Direction sortDirection, 
				Integer limit, 
				Long offset, 
				String q,
				 Long userId,
				 Long serviceId,
				 Long organizationId, 
				 OffsetDateTime creationDateFrom, 
				 OffsetDateTime creationDateTo) {
		
		this.authService.expectAnyRole(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN, GovIORoles.RUOLO_GOVIO_SENDER, GovIORoles.RUOLO_GOVIO_VIEWER);
		
		Specification<GovioFileEntity> spec = GovioFileFilters.empty();
		
		if (userId != null) {
			spec = spec.and(GovioFileFilters.byUser(userId));
		}
		if (serviceId != null) {
			spec = spec.and(GovioFileFilters.byService(serviceId));
		}
		if (organizationId != null) {
			spec = spec.and(GovioFileFilters.byOrganization(organizationId));
		}
		if (q != null) {
			spec = spec.and(GovioFileFilters.likeFileName(q));
		}
		if(creationDateFrom != null) {
			spec = spec.and(GovioFileFilters.fromCreationDate(creationDateFrom));
		}
		if(creationDateTo != null) {
			spec = spec.and(GovioFileFilters.untilCreationDate(creationDateTo));
		}
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, GovioFileFilters.sort(sortDirection));
		
		FileList ret = traceService.listFiles(spec, pageRequest);
		
		return ResponseEntity.ok(ret);
	}


	@Override
	public ResponseEntity<GovIOFile> readCsvTrace(Long traceId) {
		// TODO: Come popolo errorMessages e aquiredMessages?
		// AquiredMessages sarà il numero di govio_file_messages collegata a una govio_files
		//  ErrorMessages sarà il numero in cui GovioFileMessageEntity collegati al govio_file_messages per cui error è a null

		this.authService.expectAnyRole(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN, GovIORoles.RUOLO_GOVIO_SENDER, GovIORoles.RUOLO_GOVIO_VIEWER);
				
		return ResponseEntity.ok(	this.traceService.readFile(traceId));
	}


	@Override
	public ResponseEntity<Resource> readCsvFileContent(Long id) {
	
    	this.authService.expectAnyRole(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN, GovIORoles.RUOLO_GOVIO_SENDER, GovIORoles.RUOLO_GOVIO_VIEWER);
    	
    	GovioFileEntity file = this.fileRepo.findById(id)
    			.orElseThrow( () -> new ResourceNotFoundException("File di id ["+id+"] non trovato."));
		
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
		ResponseEntity<Resource> ret =   new ResponseEntity<>(fileStream, headers, HttpStatus.OK); 
		
		return ret;
	}


	@Override
	public ResponseEntity<FileMessageList> readFileMessages(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

}
