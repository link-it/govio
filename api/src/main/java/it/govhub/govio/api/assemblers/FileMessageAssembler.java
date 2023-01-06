package it.govhub.govio.api.assemblers;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govio.api.beans.FileMessage;
import it.govhub.govio.api.beans.FileMessageStatusEnum;
import it.govhub.govio.api.entity.GovioFileMessageEntity;
import it.govhub.govio.api.web.FileController;

@Component
public class FileMessageAssembler extends RepresentationModelAssemblerSupport<GovioFileMessageEntity, FileMessage>{
       
       @Autowired
       MessageItemAssembler msgItemAssembler;

       public FileMessageAssembler() {
               super(FileController.class, FileMessage.class);
       }

       @Override
       public FileMessage toModel(GovioFileMessageEntity src) {
               FileMessage ret = instantiateModel(src);
               
               FileMessageStatusEnum status = src.getGovioMessage() == null ? FileMessageStatusEnum.ERROR : FileMessageStatusEnum.ACQUIRED;
               
               BeanUtils.copyProperties(src,  ret);
               
               ret.status(status)
                       .message(this.msgItemAssembler.toModel(src.getGovioMessage()));
               
               return ret;
       }

}
