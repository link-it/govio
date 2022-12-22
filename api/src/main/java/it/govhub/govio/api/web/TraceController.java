package it.govhub.govio.api.web;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileItemHeaders;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import it.govhub.govio.api.beans.GovIOFile;
import it.govhub.govio.api.entity.ServiceInstanceEntity;
import it.govhub.govio.api.repository.ServiceInstanceEntityRepository;
import it.govhub.govio.api.services.TraceService;
import it.govhub.govio.api.spec.TraceApi;
import it.govhub.govregistry.commons.exception.BadRequestException;
import it.govhub.govregistry.commons.exception.SemanticValidationException;

@RestController
public class TraceController implements TraceApi {
	
	@Autowired
	ServiceInstanceEntityRepository serviceRepo;
	
	@Autowired
	TraceService traceService;
	
	Logger logger = LoggerFactory.getLogger(TraceController.class);
	
	/**
	 * I parametri argomento vengono ignorati e sono null. Abbiamo disabilitato la gestione del multipart di spring 
	 * in modo da poter utilizzare commons-fileupload di apache in modo da fare lo streaming della
	 * richiesta direttamente su file.
	 */
	@Override
	public ResponseEntity<GovIOFile> uploadCsvTrace(Long serviceId, Long organizationId, String name, MultipartFile file) {
		
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		
		// Leggo il body multipart ed estraggo nomeFile e stream di input
		String sourceFilename = null;
		InputStream sourceStream = null;
		
		try {
			FileItemIterator iterStream = new ServletFileUpload().getItemIterator(request);
			
			// Leggo il body multipart ed estraggo il file,
			// NOTA: la clausola del while DEVE essere scritta in questo ordine. Lo stream corrente viene invalidato
			// quando viene chiamato iterStream.hasNext.
			// Per lo short-circuit dell'&&, una volta trovato l'elemento multipart necessario, usciamo dal while
			// senza chiamare iterStream.hasNext
			
			while (sourceFilename == null && iterStream.hasNext()) {
			    FileItemStream item = iterStream.next();
			    
			    if (item.isFormField()) {
			    	logger.debug("Skipping multipart form field {}", item.getFieldName());
			    } else {
			    	sourceStream = item.openStream();
				    sourceFilename = readFilenameFromHeaders(item.getHeaders());
			    }
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
    	if (StringUtils.isEmpty(sourceFilename)) {
    		throw new BadRequestException("E' necessario indicare il filename nello header Content-Disposition del blocco multipart del file.\ne.g: [Content-Disposition: form-data; name=\"file\"; filename=\"pom.xml\"] ");
    	}
    	
    	ServiceInstanceEntity serviceInstance = this.serviceRepo.findByService_IdAndOrganization_Id(serviceId, organizationId)
    			.orElseThrow( () -> new SemanticValidationException("L'istanza di servizio indicata non esiste"));
		
    	this.traceService.uploadCSV(serviceInstance, sourceFilename, sourceStream);
		
		return ResponseEntity.ok(new GovIOFile());
	}
	
	
	private String readFilenameFromHeaders(FileItemHeaders headers) {
		
    	String filename = null;
    	try {
	    	String contentDisposition = headers.getHeader("Content-Disposition");
	    	
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
	
}
