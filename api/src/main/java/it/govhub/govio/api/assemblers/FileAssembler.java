package it.govhub.govio.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import it.govhub.govio.api.beans.EmbedFileEnum;
import it.govhub.govio.api.beans.EmbedMessageEnum;
import it.govhub.govio.api.beans.EmbedServiceInstanceEnum;
import it.govhub.govio.api.beans.GovioFile;
import it.govhub.govio.api.beans.GovioMessage;
import it.govhub.govio.api.entity.GovioFileEntity;
import it.govhub.govio.api.web.FileController;
import it.govhub.govio.api.web.ServiceInstanceController;
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
	
	@Autowired
	ServiceInstanceAssembler instanceAssembler;

	Logger log = LoggerFactory.getLogger(FileAssembler.class);

	public FileAssembler() {
		super(FileController.class, GovioFile.class);
	}

	@Override
	public GovioFile  toModel(GovioFileEntity src) {
		log.debug("Assembling Entity [GovioFile] to model...");
		GovioFile ret = instantiateModel(src);
		
        BeanUtils.copyProperties(src, ret);

        ret.serviceInstanceId(src.getServiceInstance().getId())
            .user(this.userAssembler.toModel(src.getGovauthUser()))
            .status(src.getStatus())
            .filename(src.getName());

		ret.
				add(linkTo(
							methodOn(FileController.class).
							readFile(src.getId())).
						withSelfRel()).
				add(linkTo(
							methodOn(FileController.class).
							readFileContent(src.getId())).
						 withRel("content")).
				add(linkTo(
						methodOn(ServiceInstanceController.class).
						readServiceInstance(src.getServiceInstance().getId())).
					withRel("service-instance")
					);
				
		return ret;
	}

	public GovioFile toEmbeddedModel(GovioFileEntity src, Collection<EmbedFileEnum> embed) {
		
		GovioFile ret = this.toModel(src);
		
		if (!CollectionUtils.isEmpty(embed)) {
			embed = new HashSet<>(embed);
			ret.setEmbedded(new HashMap<>());
			
			for(var e : embed) {
				switch(e) {
				case SERVICE_INSTANCE:
					ret.getEmbedded().put(
							EmbedMessageEnum.SERVICE_INSTANCE.getValue(), 
							this.instanceAssembler.toEmbeddedModel(src.getServiceInstance(), Set.of(EmbedServiceInstanceEnum.values()))
						);
					break;
				default:
					break; 
				}
			}
		}
		
		return ret;
	}
	
}
