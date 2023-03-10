-- Applicazioni

INSERT INTO public.govhub_applications (id, application_id, name, deployed_uri) VALUES (2, 'govio', 'GovIO', 'http://localhost/govio');

-- Utenze

INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (1, 'amministratore', 'Amministratore Vanguard', 'admin@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (2, 'ospite', 'Ospite Calmo', 'ospite@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (3, 'govio_sender', 'Vincenzo Traccia', 'govio_sender@govhub.it', true);

ALTER SEQUENCE SEQ_GOVHUB_USERS RESTART WITH 4;

-- Ruoli

INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (1, (select id from govhub_applications where application_id='govio'), 'govio_sysadmin');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (2, (select id from govhub_applications where application_id='govio'), 'govio_sender');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (3, (select id from govhub_applications where application_id='govio'), 'govio_viewer');

ALTER SEQUENCE SEQ_GOVHUB_ROLES RESTART WITH 4;

-- Organizations

INSERT INTO public.govhub_organizations (id, tax_code, legal_name) VALUES (1, '12345678901', 'Ente Creditore');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name) VALUES (2, '12345678902', 'Ente Creditore 2');

ALTER SEQUENCE SEQ_GOVHUB_ORGANIZATIONS RESTART WITH 3;

-- Services

INSERT INTO public.govhub_services (id, name, description) VALUES (1, 'Servizio Generico', 'Esempio di servizio');
INSERT INTO public.govhub_services (id, name, description) VALUES (2, 'Servizio senza autorizzazioni', 'Servizio non autorizzato');
INSERT INTO public.govhub_services (id, name, description) VALUES (3, 'SUAP-Integrazione', 'Service for customer management');
INSERT INTO public.govhub_services (id, name, description) VALUES (4, 'IMU-ImpostaMunicipaleUnica', 'Imposta municipale unica');
INSERT INTO public.govhub_services (id, name, description) VALUES (5, 'TARI', 'Tassa sui rifiuti');
INSERT INTO public.govhub_services (id, name, description) VALUES (6, 'Portale ZTL', 'Servizio di registrazione accessi ZTL comunale');
INSERT INTO public.govhub_services (id, name, description) VALUES (7, 'Variazione Residenza', 'Richieste di variazione residenza');
INSERT INTO public.govhub_services (id, name, description) VALUES (8, 'Servizi Turistici', 'Portale di riferimento per i turisti');

ALTER SEQUENCE SEQ_GOVHUB_SERVICES RESTART WITH 9;

-- Autorizzazioni

-- amministratore -> govio_sysadmin
INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (1, 1, 1);

-- govio_sender -> govio_sender
INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (2, 3, 2);

ALTER SEQUENCE SEQ_GOVHUB_AUTHORIZATIONS RESTART WITH 3;


-- Servizio di test

INSERT INTO govio_templates(id,message_body,subject,has_due_date,has_payment) VALUES (1,'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur','Lorem ipsum dolor sit amet.',false,false);
ALTER SEQUENCE seq_govio_templates RESTART WITH 2;	


INSERT INTO govio_service_instances(id,id_govhub_service,id_govhub_organization,id_govio_template,apikey) VALUES (1,1,1,1,'17886617e07d47e8b1ba314f2f1e3052');
INSERT INTO govio_service_instances(id,id_govhub_service,id_govhub_organization,id_govio_template,apikey) VALUES (2,4,2,1,'17886617e07d47e8b1ba314f2f1e3053');

ALTER SEQUENCE seq_govio_service_instances RESTART WITH 3;

-- INSERT INTO govio_placeholders(id,name,type,example) VALUES (1,'appointment','DATE','2100-12-31T12:00');
-- INSERT INTO govio_placeholders(id,name,type,example) VALUES (2,'at','STRING','Ufficio numero 1');
-- ALTER SEQUENCE seq_govio_placeholders RESTART WITH 3;

-- INSERT INTO govio_template_placeholders(id_govio_template,id_govio_placeholder, mandatory, position) VALUES ('1','1', true, 1);
-- INSERT INTO govio_template_placeholders(id_govio_template,id_govio_placeholder, mandatory, position) VALUES ('1','2', true, 2);




























