package it.govhub.govio.api.test.costanti;

import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;

public class Costanti {

	public static final String PARAMETRO_SERVICE_ID = "service_id";
	public static final String PARAMETRO_ORGANIZATION_ID = "organization_id";
	public static final String TEXT_CSV_CONTENT_TYPE = "text/csv";
	
	public static final String TAX_CODE_ENTE_CREDITORE_2 = "12345678902";
	public static final String LEGALNAME_ENTE_CREDITORE_2 = "Ente Creditore 2";
	
	public static final String USERS_QUERY_PARAM_LIMIT = "limit";
	public static final String USERS_QUERY_PARAM_OFFSET = "offset";
	public static final String USERS_QUERY_PARAM_Q = "q";
	public static final String USERS_QUERY_PARAM_ENABLED = "enabled";
	public static final String USERS_QUERY_PARAM_SORT = "sort";
	public static final String USERS_QUERY_PARAM_SORT_DIRECTION = "sort_direction";
	
	public static final Integer USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE = LimitOffsetPageRequest.LIMIT_DEFAULT_VALUE;
	
	public static final String QUERY_PARAM_SORT_DIRECTION_ASC = "asc";
	public static final String QUERY_PARAM_SORT_DIRECTION_DESC = "desc";
	
	
	public static final String SERVICE_NAME_SERVIZIO_GENERICO = "Servizio Generico";
	public static final String SERVICE_DESCRIPTION_SERVIZIO_GENERICO = "Esempio di servizio";
	
	/* ORGANIZATIONS */
	
	public static OrganizationEntity getEnteCreditore2() {
		return OrganizationEntity.builder()
				.taxCode(Costanti.TAX_CODE_ENTE_CREDITORE_2)
				.legalName(Costanti.LEGALNAME_ENTE_CREDITORE_2)
				.build();
	}
	/* SERVICES */
	
	public static ServiceEntity getServizioGenerico() {
		return ServiceEntity.builder()
				.name(Costanti.SERVICE_NAME_SERVIZIO_GENERICO)
				.description(Costanti.SERVICE_DESCRIPTION_SERVIZIO_GENERICO)
				.build();
	}
}
