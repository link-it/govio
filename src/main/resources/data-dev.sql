-- Applicazione
INSERT INTO govhub_applications(id, application_id, name, deployed_uri) VALUES (2, 'govio', 'GovIO', 'http://localhost:10002');

-- Utenti con ruoli che vanno su tutti i comuni e i servizi

INSERT INTO govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'user_govio_sender', 'Marco Sender', 'govio_sender@govio.it', true);
INSERT INTO govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'user_govio_viewer', 'Angovio Visore', 'govio_viewer@govio.it', true);
INSERT INTO govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'user_govio_instance_viewer', 'Utente Test Instance Viewer', 'govio_instance_viewer@govio.it', true);
INSERT INTO govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'user_govio_instance_editor', 'Utente Test Instance Editor', 'govio_instance_editor@govio.it', true);

-- User che lavora su un solo comune e tutti i servizi

INSERT INTO govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'user_govio_roma_instance_editor', 'Utente Test Per Roma', 'govio_roma_instance_editor@govio.it', true);

-- User che lavora su tutti i comuni e un solo servizio

INSERT INTO govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'user_govio_cie_instance_editor', 'Utente Test PER CIE', 'govio_cie_instance_editor@govio.it', true);

-- User che lavora su un solo comune e un solo servizio

INSERT INTO govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'user_cie_roma', 'Utente Test per CIE - Roma', 'user-cie-roma@govio.it', true);

-- Ruoli


INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govio'), 'govio_sender');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govio'), 'govio_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govio'), 'govio_sysadmin');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govio'), 'govio_service_instance_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govio'), 'govio_service_instance_editor');

-- Creo un govio_service

INSERT INTO govhub_services(id, name, description) VALUES(nextval('seq_govhub_services'), 'CIE', 'Emissione carta d''identità Elettronica');

INSERT INTO govhub_services(id, name, description) VALUES(nextval('seq_govhub_services'), 'Servizio Dimostrativo', 'Servizio utilizzato per la dimostrazione dell''app GovIO');

INSERT INTO govhub_organizations(id, tax_code, legal_name) VALUES(nextval('seq_govhub_organizations'), '80015010723', 'Roma');

INSERT INTO govhub_organizations(id, tax_code, legal_name) VALUES(nextval('seq_govhub_organizations'), '84576116611', 'Comune Dimostrativo');


-- amministratore -> govio_sysadmin

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (
	nextval('public.seq_govhub_authorizations'), 
	(SELECT id FROM public.govhub_users WHERE principal='amministratore'), 
	(SELECT id FROM public.govhub_roles WHERE name='govio_sysadmin')
);


INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (
	nextval('public.seq_govhub_authorizations'), 
	(SELECT id FROM public.govhub_users WHERE principal='user_govio_sender'), 
	(SELECT id FROM public.govhub_roles WHERE name='govio_sender')
);


INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (
	nextval('public.seq_govhub_authorizations'), (
		SELECT id FROM public.govhub_users WHERE principal='user_govio_viewer'), (
		SELECT id FROM public.govhub_roles WHERE name='govio_viewer')
);


INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (
	nextval('public.seq_govhub_authorizations'), 
	(SELECT id FROM public.govhub_users WHERE principal='user_govio_instance_editor'), 
	(SELECT id FROM public.govhub_roles WHERE name='govio_service_instance_editor')
);


INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (
	nextval('public.seq_govhub_authorizations'), 
	(SELECT id FROM public.govhub_users WHERE principal='user_govio_instance_viewer'), 
	(SELECT id FROM public.govhub_roles WHERE name='govio_service_instance_viewer')
);



-- inserimento dati per la configurazione di un template per la spedizione di un messaggio

do $$
declare
	auth_id integer;
	template_id integer;
	placeholder_id integer;
    service_instance_id integer;
begin

	-- Popolo le autorizzazioni 

	INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (
		nextval('public.seq_govhub_authorizations'), 
		(SELECT id FROM public.govhub_users WHERE principal='user_govio_roma_instance_editor'), 
		(SELECT id FROM public.govhub_roles WHERE name='govio_service_instance_editor')) returning id into auth_id;
	INSERT INTO govhub_auth_organizations (id_govhub_authorization, id_govhub_organization) VALUES (
		auth_id,
		(SELECT id FROM govhub_organizations WHERE legal_name = 'Roma')
	);


	INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (
		nextval('public.seq_govhub_authorizations'), 
		(SELECT id FROM public.govhub_users WHERE principal='user_govio_cie_instance_editor'), 
		(SELECT id FROM public.govhub_roles WHERE name='govio_service_instance_editor')) returning id into auth_id;
	INSERT INTO govhub_auth_services (id_govhub_authorization, id_govhub_service) VALUES (
		auth_id,
		(SELECT id FROM govhub_services WHERE name = 'CIE')
	);

	INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (
		nextval('public.seq_govhub_authorizations'), 
		(SELECT id FROM public.govhub_users WHERE principal='user_cie_roma'), 
		(SELECT id FROM public.govhub_roles WHERE name='govio_service_instance_editor')) returning id into auth_id;
	INSERT INTO govhub_auth_services (id_govhub_authorization, id_govhub_service) VALUES (
		auth_id,
		(SELECT id FROM govhub_services WHERE name = 'CIE')
	);
	INSERT INTO govhub_auth_organizations (id_govhub_authorization, id_govhub_organization) VALUES (
		auth_id,
		(SELECT id FROM govhub_organizations WHERE legal_name = 'Roma')
	);




	INSERT INTO govio_templates(id, message_body, subject, has_due_date, has_payment) VALUES (
		nextval('public.seq_govio_templates'), 
		'Salve, con la presente la informiamo che in data ${due_date} scadrà la Carta di Identità elettronica numero ${cie.uppercase}. Per maggiori informazioni sulle modalità di rinnovo può consultare https://comune.dimostrativo.it.', 
		'Scadenza CIE n. ${cie.uppercase}', 
		true, 
		false) returning id into template_id;

	INSERT INTO govio_placeholders(id, name, type, example) VALUES (
		nextval('public.seq_govio_placeholders'), 
		'cie', 
		'STRING', 
		'CA000000AA') returning id into placeholder_id;

	INSERT INTO govio_template_placeholders(id_govio_template, id_govio_placeholder, mandatory, position) VALUES (template_id, placeholder_id, true, 1);


	INSERT INTO govio_service_instances(id, id_govhub_service, id_govhub_organization, id_govio_template,apikey) VALUES (
		nextval('public.seq_govio_service_instances'), 
		(select id from govhub_services where name='CIE'),
		(select id from govhub_organizations where tax_code='80015010723'),
		template_id,
		'17886617e07d47e8b1ba314f2f1e3052');
end $$


