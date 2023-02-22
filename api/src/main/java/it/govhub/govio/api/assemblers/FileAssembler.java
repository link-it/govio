package it.govhub.govio.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govio.api.beans.GovioFile;
import it.govhub.govio.api.entity.GovioFileEntity;
import it.govhub.govio.api.web.FileController;
import it.govhub.govregistry.readops.api.assemblers.OrganizationAuthItemAssembler;
import it.govhub.govregistry.readops.api.assemblers.ServiceAuthItemAssembler;


@Component
public class FileAssembler  extends RepresentationModelAssemblerSupport<GovioFileEntity, GovioFile> {
	
	@Autowired
	FileUserItemAssembler userAssembler;
	
	@Autowired
	OrganizationAuthItemAssembler orgAssembler;
	
	@Autowired
	ServiceAuthItemAssembler serviceAssembler;

	Logger log = LoggerFactory.getLogger(FileAssembler.class);

	public FileAssembler() {
		super(FileController.class, GovioFile.class);
	}

	@Override
	public GovioFile  toModel(GovioFileEntity src) {
		log.debug("Assembling Entity [GovioFile] to model...");
		GovioFile ret = instantiateModel(src);
		
        BeanUtils.copyProperties(src, ret);

        ret.organization(this.orgAssembler.toModel(src.getServiceInstance().getOrganization()))
        	.service(this.serviceAssembler.toModel(src.getServiceInstance().getService().getGovhubService()))
            .user(this.userAssembler.toModel(src.getGovauthUser()))
            .status(src.getStatus())
            .filename(src.getName());

		ret.add(linkTo(
					methodOn(FileController.class)
					.readFile(src.getId()))
				.withSelfRel()
			).add(linkTo(
					methodOn(FileController.class)
					.readFileContent(src.getId()))
				.withRel("content")
				);
				
		return ret;
	}
	
}
