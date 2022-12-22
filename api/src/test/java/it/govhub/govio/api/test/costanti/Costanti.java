package it.govhub.govio.api.test.costanti;

import org.apache.commons.codec.binary.Base64;

import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;

public class Costanti {

	public static final String PARAMETRO_SERVICE_ID = "service_id";
	public static final String PARAMETRO_ORGANIZATION_ID = "organization_id";
	public static final String TEXT_CSV_CONTENT_TYPE = "text/csv";
	
	public static final String TAX_CODE_ENTE_CREDITORE_3 = "12345678903";
	public static final String LEGALNAME_ENTE_CREDITORE_3 = "Ente Creditore 3";
	public static final byte[] LOGO_ENTE_CREDITORE_3 = "iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAApgAAAKYB3X3/OAAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAANCSURBVEiJtZZPbBtFFMZ/M7ubXdtdb1xSFyeilBapySVU8h8OoFaooFSqiihIVIpQBKci6KEg9Q6H9kovIHoCIVQJJCKE1ENFjnAgcaSGC6rEnxBwA04Tx43t2FnvDAfjkNibxgHxnWb2e/u992bee7tCa00YFsffekFY+nUzFtjW0LrvjRXrCDIAaPLlW0nHL0SsZtVoaF98mLrx3pdhOqLtYPHChahZcYYO7KvPFxvRl5XPp1sN3adWiD1ZAqD6XYK1b/dvE5IWryTt2udLFedwc1+9kLp+vbbpoDh+6TklxBeAi9TL0taeWpdmZzQDry0AcO+jQ12RyohqqoYoo8RDwJrU+qXkjWtfi8Xxt58BdQuwQs9qC/afLwCw8tnQbqYAPsgxE1S6F3EAIXux2oQFKm0ihMsOF71dHYx+f3NND68ghCu1YIoePPQN1pGRABkJ6Bus96CutRZMydTl+TvuiRW1m3n0eDl0vRPcEysqdXn+jsQPsrHMquGeXEaY4Yk4wxWcY5V/9scqOMOVUFthatyTy8QyqwZ+kDURKoMWxNKr2EeqVKcTNOajqKoBgOE28U4tdQl5p5bwCw7BWquaZSzAPlwjlithJtp3pTImSqQRrb2Z8PHGigD4RZuNX6JYj6wj7O4TFLbCO/Mn/m8R+h6rYSUb3ekokRY6f/YukArN979jcW+V/S8g0eT/N3VN3kTqWbQ428m9/8k0P/1aIhF36PccEl6EhOcAUCrXKZXXWS3XKd2vc/TRBG9O5ELC17MmWubD2nKhUKZa26Ba2+D3P+4/MNCFwg59oWVeYhkzgN/JDR8deKBoD7Y+ljEjGZ0sosXVTvbc6RHirr2reNy1OXd6pJsQ+gqjk8VWFYmHrwBzW/n+uMPFiRwHB2I7ih8ciHFxIkd/3Omk5tCDV1t+2nNu5sxxpDFNx+huNhVT3/zMDz8usXC3ddaHBj1GHj/As08fwTS7Kt1HBTmyN29vdwAw+/wbwLVOJ3uAD1wi/dUH7Qei66PfyuRj4Ik9is+hglfbkbfR3cnZm7chlUWLdwmprtCohX4HUtlOcQjLYCu+fzGJH2QRKvP3UNz8bWk1qMxjGTOMThZ3kvgLI5AzFfo379UAAAAASUVORK5CYII=".getBytes();
	public static final byte[] LOGO_MINIATURA_ENTE_CREDITORE_3 = LOGO_ENTE_CREDITORE_3;
	
	public static final String TAX_CODE_ENTE_CREDITORE_4 = "12345678904";
	public static final String LEGALNAME_ENTE_CREDITORE_4 = "Ente Creditore 4";
	
	public static final String USERS_QUERY_PARAM_LIMIT = "limit";
	public static final String USERS_QUERY_PARAM_OFFSET = "offset";
	public static final String USERS_QUERY_PARAM_Q = "q";
	public static final String USERS_QUERY_PARAM_ENABLED = "enabled";
	public static final String USERS_QUERY_PARAM_SORT = "sort";
	public static final String USERS_QUERY_PARAM_SORT_DIRECTION = "sort_direction";
	
	public static final Integer USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE = LimitOffsetPageRequest.LIMIT_DEFAULT_VALUE;
	
	public static final String QUERY_PARAM_SORT_DIRECTION_ASC = "asc";
	public static final String QUERY_PARAM_SORT_DIRECTION_DESC = "desc";
	
	
	public static final String SERVICE_NAME_TEST = "Servizio Test";
	public static final String SERVICE_DESCRIPTION_TEST = "Generica descrizione per un servizio di test";
	
	/* ORGANIZATIONS */
	
	public static OrganizationEntity getEnteCreditore3() {
		return OrganizationEntity.builder()
				.taxCode(Costanti.TAX_CODE_ENTE_CREDITORE_3)
				.legalName(Costanti.LEGALNAME_ENTE_CREDITORE_3)
				.officeAddress("Piazza Italia 1")
				.officeAddressDetails("Piazza Italia 1")
				.officeAt("Interno 1")
				.officeEmailAddress("entecreditore3@email.com")
				.officeForeignState("Italy")
				.officeMunicipality("Roma")
				.officeMunicipalityDetails("Municipio 3")
				.officePecAddress("entecreditore3@pec.it")
				.officePhoneNumber("00 1234 5678")
				.officeProvince("RO")
				.officeZip("0000")
				.logo(Base64.decodeBase64(LOGO_ENTE_CREDITORE_3))
				.logoMiniature(Base64.decodeBase64(LOGO_MINIATURA_ENTE_CREDITORE_3))
				.build();
	}
	
	public static OrganizationEntity getEnteCreditore4() {
		return OrganizationEntity.builder()
				.taxCode(Costanti.TAX_CODE_ENTE_CREDITORE_4)
				.legalName(Costanti.LEGALNAME_ENTE_CREDITORE_4)
				.build();
	}
	/* SERVICES */
	
	public static ServiceEntity getServizioTest() {
		return ServiceEntity.builder()
				.name(Costanti.SERVICE_NAME_TEST)
				.description(Costanti.SERVICE_DESCRIPTION_TEST)
				.build();
	}
}
