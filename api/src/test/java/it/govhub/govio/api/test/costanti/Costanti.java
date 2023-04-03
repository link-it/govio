package it.govhub.govio.api.test.costanti;

import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;

public class Costanti {
	
	public static final String STRING_256 = "abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234";

	public final static char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	public static final String PART_NAME_FILE = "file";
	

	public static final String PARAMETRO_SERVICE_ID = "service_id";
	public static final String PARAMETRO_ORGANIZATION_ID = "organization_id";
	public static final String PARAMETRO_SERVICE_INSTANCE_ID = "service_instance";
	public static final String TEXT_CSV_CONTENT_TYPE = "text/csv";
	
	public static final String TAX_CODE_ENTE_CREDITORE = "12345678901";
	public static final String LEGALNAME_ENTE_CREDITORE = "Ente Creditore";
	
	public static final String TAX_CODE_ENTE_CREDITORE_3 = "12345678903";
	public static final String TAX_CODE_ENTE_CREDITORE_2 = "12345678902";
	public static final String TAX_CODE_CIE_ORG = "80015010723";
	
	public static final String USERS_QUERY_PARAM_LIMIT = "limit";
	public static final String USERS_QUERY_PARAM_OFFSET = "offset";
	public static final String USERS_QUERY_PARAM_Q = "q";
	public static final String USERS_QUERY_PARAM_ENABLED = "enabled";
	public static final String USERS_QUERY_PARAM_SORT = "sort";
	public static final String USERS_QUERY_PARAM_SORT_DIRECTION = "sort_direction";
	public static final String USERS_QUERY_PARAM_ROLES = "with_roles";
	public static final String USERS_QUERY_PARAM_WITH_SERVICE_INSTANCE = "with_service_instance";
	public static final String USERS_QUERY_PARAM_SERVICE_ID = "service_id";
	public static final String USERS_QUERY_PARAM_ORGANIZATION_ID = "organization_id";
	public static final String USERS_QUERY_PARAM_EMBED = "embed";
	public static final String USERS_QUERY_PARAM_PLACEHOLDER_ID = "placeholder_id";
	
	public static final Integer USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE = LimitOffsetPageRequest.LIMIT_DEFAULT_VALUE;
	
	public static final String QUERY_PARAM_SORT_DIRECTION_ASC = "asc";
	public static final String QUERY_PARAM_SORT_DIRECTION_DESC = "desc";
	
	public static final String SERVICE_NAME_SERVIZIO_GENERICO = "Servizio Generico";
	public static final String SERVICE_DESCRIPTION_SERVIZIO_GENERICO = "Esempio di servizio";
	
	public static final String SERVICE_NAME_TARI = "TARI";
	public static final String SERVICE_NAME_CIE = "CIE"; 
	public static final String SERVICE_IMU = "IMU-ImpostaMunicipaleUnica";
	
	public static final String FILES_QUERY_PARAM_CREATION_DATE_FROM = "creation_date_from";
	public static final String FILES_QUERY_PARAM_CREATION_DATE_TO = "creation_date_to";
	public static final String FILES_QUERY_PARAM_USER_ID = "user_id";
	public static final String FILES_QUERY_PARAM_SERVICE_ID = "service_id";
	public static final String FILES_QUERY_PARAM_ORGANIZATION_ID = "organization_id";
	
	public static final String FILES_QUERY_PARAM_LINE_NUMBER_FROM = "line_number_from";
	public static final String FILES_QUERY_PARAM_FILE_MESSAGE_STATUS = "file_message_status";
	
	public static final String TEMPLATE_CIE_SUBJECT = "Scadenza CIE n. ${cie}";
	public static final String TEMPLATE_CIE_MESSAGE_BODY = "Salve, con la presente la informiamo che in data ${due_date} scadrà la Carta di Identità elettronica numero ${cie}. Per maggiori informazioni sulle modalità di rinnovo può consultare https://comune.dimostrativo.it.";
	
	
	
	/* ORGANIZATIONS */
	
	public static OrganizationEntity getEnteCreditore() {
		return OrganizationEntity.builder()
				.taxCode(Costanti.TAX_CODE_ENTE_CREDITORE)
				.legalName(Costanti.LEGALNAME_ENTE_CREDITORE)
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
