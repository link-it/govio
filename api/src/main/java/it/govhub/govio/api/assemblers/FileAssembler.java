package it.govhub.govio.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govio.api.beans.GovIOFile;
import it.govhub.govio.api.entity.GovIOFileEntity;
import it.govhub.govio.api.web.TraceController;
import it.govhub.govregistry.commons.assemblers.OrganizationAuthItemAssembler;
import it.govhub.govregistry.commons.assemblers.ServiceAuthItemAssembler;

@Component
public class FileAssembler  extends RepresentationModelAssemblerSupport<GovIOFileEntity, GovIOFile> {
	
	@Autowired
	FileUserItemAssembler userAssembler;
	
	@Autowired
	OrganizationAuthItemAssembler orgAssembler;
	
	@Autowired
	ServiceAuthItemAssembler serviceAssembler;
	

	public FileAssembler() {
		super(TraceController.class, GovIOFile.class);
	}

	@Override
	public GovIOFile  toModel(GovIOFileEntity src) {
		GovIOFile ret = instantiateModel(src);
		
        BeanUtils.copyProperties(src, ret);
        
        ret.acquiredMessages(null)
        	.errorMessages(null)
        	.organization(this.orgAssembler.toModel(src.getServiceInstance().getOrganization()))
        	.service(this.serviceAssembler.toModel(src.getServiceInstance().getService()))
            .user(this.userAssembler.toModel(src.getGovauthUser()))
            .filename(src.getName());

		ret.add(linkTo(
					methodOn(TraceController.class)
					.readCsvTrace(src.getId()))
				.withSelfRel()
			).add(linkTo(
					methodOn(TraceController.class)
					.readCsvFileContent(src.getId()))
				.withRel("content")
				);
				
		return ret;
	}
	
}
