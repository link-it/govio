package it.govhub.govio.api.web;

import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;

import it.govhub.govio.api.beans.GovioMessage;
import it.govhub.govio.api.beans.GovioMessageList;
import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.repository.GovioMessageFilters;
import it.govhub.govio.api.services.MessageService;
import it.govhub.govio.api.spec.MessageApi;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;

@V1RestController
public class MessageController implements MessageApi {
	
	@Autowired
	MessageService messageService;

	@Override
	public ResponseEntity<GovioMessageList> listMessages(
			OffsetDateTime scheduledExpeditionDateFrom,
			OffsetDateTime scheduledExpeditionDateTo,
			OffsetDateTime expeditionDateFrom,
			OffsetDateTime expeditionDateTo,
			String taxCode,
			Long serviceId,
			Long organizationId,
			Integer limit,
			Long offset) {
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, Sort.unsorted());	// TODO: Sort
		
		Specification<GovioMessageEntity> spec = GovioMessageFilters.empty();
		if (scheduledExpeditionDateFrom != null) {
			spec = spec.and(GovioMessageFilters.fromScheduledExpeditionDate(scheduledExpeditionDateFrom));
		}
		if (scheduledExpeditionDateTo != null) {
			spec = spec.and(GovioMessageFilters.toScheduledExpeditionDate(scheduledExpeditionDateTo));
		}
		if (expeditionDateFrom != null) {
			spec = spec.and(GovioMessageFilters.fromExpeditionDate(expeditionDateFrom));
		}
		if (expeditionDateTo != null) {
			spec = spec.and(GovioMessageFilters.toExpeditionDate(expeditionDateTo));
		}
		if (taxCode != null) {
			spec = spec.and(GovioMessageFilters.byTaxCode(taxCode));
		}
		if (serviceId != null) {
			spec = spec.and(GovioMessageFilters.byServiceId(serviceId));
		}
		if (organizationId != null) {
			spec = spec.and(GovioMessageFilters.byOrganizationId(organizationId));
		}
		
		// TODO: Autorizzazioni
		GovioMessageList ret = this.messageService.listMessages(spec, pageRequest);
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<GovioMessage> readMessage(Long id) {
		
		// TODO: Autorizzazioni
		return ResponseEntity.ok(this.messageService.readMessage(id));
	}

}
