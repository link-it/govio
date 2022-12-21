package it.govhub.govio.api.security;

import java.util.Set;

public class GovIORoles {

	public static final String REALM_NAME = "govio";
	public static final String RUOLO_GOVIO_SENDER = "govio_sender"; 
	public static final String RUOLO_GOVIO_VIEWER = "govio_viewer"; 

	
	// impostarli nel componente jee utilizzando la funzione mappableAuthorities al posto di mappableRoles che aggiunge il prefisso 'ROLE_' ad ogni ruolo
	public static final Set<String> ruoliConsentiti = Set.of
			( 
				RUOLO_GOVIO_SENDER,
				RUOLO_GOVIO_VIEWER
			);

	
	private GovIORoles() {		}

}
