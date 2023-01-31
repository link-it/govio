package it.govhub.govio.api.web;

import java.util.List;

import org.springframework.http.ResponseEntity;

import it.govhub.govio.api.beans.EmbedPlaceholderEnum;
import it.govhub.govio.api.beans.GovioNewPlaceholder;
import it.govhub.govio.api.beans.GovioNewTemplate;
import it.govhub.govio.api.beans.GovioNewTemplatePlaceholder;
import it.govhub.govio.api.beans.GovioPlaceholder;
import it.govhub.govio.api.beans.GovioPlaceholderList;
import it.govhub.govio.api.beans.GovioTemplate;
import it.govhub.govio.api.beans.GovioTemplateList;
import it.govhub.govio.api.beans.GovioTemplatePlaceholder;
import it.govhub.govio.api.spec.TemplateApi;
import it.govhub.govregistry.commons.config.V1RestController;

@V1RestController
public class TemplateController implements TemplateApi {

	@Override
	public ResponseEntity<GovioTemplate> createTemplate(GovioNewTemplate govioNewTemplate) {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public ResponseEntity<GovioPlaceholder> createPlaceholder(GovioNewPlaceholder govioNewPlaceholder) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ResponseEntity<GovioTemplatePlaceholder> assignPlaceholder(Long templateId, Long placeholderId, GovioNewTemplatePlaceholder govioNewTemplatePlaceholder) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ResponseEntity<List<GovioTemplatePlaceholder>> listTemplatePlaceholders( Long templateId, EmbedPlaceholderEnum embed) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ResponseEntity<GovioTemplateList> listTemplates(Integer limit, Long offset) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ResponseEntity<GovioPlaceholderList> listPlaceholders(Integer limit, Long offset) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ResponseEntity<GovioTemplate> readTemplate(Long id) {
		// TODO Auto-generated method stub
		return null;
	}



}
