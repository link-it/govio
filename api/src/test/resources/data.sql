-- Utenze

INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (1, 'amministratore', 'Amministratore Vanguard', 'admin@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (2, 'ospite', 'Ospite Calmo', 'ospite@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (3, 'user_viewer', 'Lurker Skywalker', 'user_viewer@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (4, 'user_editor', 'User Editor', 'user_admin@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (5, 'org_viewer', 'Visore Antonio', 'org_viewer@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (6, 'org_editor', 'Giovanni Mele', 'org_editor@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (7, 'service_viewer', 'Luca Viso', 'service_viewer@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (8, 'service_editor', 'Angelo Monti', 'service_editor@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (9, 'govio_sender', 'Vincenzo Traccia', 'govio_sender@govhub.it', true);

ALTER SEQUENCE SEQ_GOVHUB_USERS RESTART WITH 10;

-- Ruoli

INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (1, 1, 'govhub_sysadmin');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (2, 1, 'govhub_users_editor');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (3, 1, 'govhub_users_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (4, 1, 'govhub_user');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (5, 1, 'govhub_organizations_editor');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (6, 1, 'govhub_organizations_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (7, 1, 'govhub_services_editor');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (8, 1, 'govhub_services_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (9, 1, 'govhub_ruolo_non_assegnabile');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (10, 1, 'govio_sender');

ALTER SEQUENCE SEQ_GOVHUB_ROLES RESTART WITH 11;

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

-- amministratore -> govhub_sysadmin
INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (1, 1, 1);

-- user_viewer -> govhub_user_viewer
INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (2, 3, 3);

-- user_editor -> govhub_users_editor
INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (3, 4, 2);

-- org_viewer -> govhub_organizations_viewer
INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (4, 5, 6);

-- org_editor -> govhub_organizations_editor
INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (5, 6, 5);

-- service_viewer -> govhub_services_viewer
INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (6, 7, 8);

-- service_editor -> govhub_services_editor
INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (7, 8, 7);

-- govio_sender -> govio_sender
INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (9, 9, 10);

ALTER SEQUENCE SEQ_GOVHUB_AUTHORIZATIONS RESTART WITH 10;

-- Ruoli assegnabili da altri ruoli

-- govhub_users_editor puo' assegnare govhub_users_viewer
INSERT INTO public.govhub_assignable_roles (role_id, assignable_role_id) VALUES (2, 3);
-- govhub_users_editor puo' assegnare govhub_user
INSERT INTO public.govhub_assignable_roles (role_id, assignable_role_id) VALUES (2, 4);


-- Servizio di test

-- INSERT INTO govio_templates(id,message_body,subject,has_due_date,has_payment) VALUES (1,'Il Comune la informa che il ${duedate.date} scadrà la sua Carta di Identità. Potrà rinnovarlo il ${appointment.verbose} presso {at}.','Scadenza CIE ${taxcode}',true,false);
INSERT INTO govio_templates(id) VALUES (1);
ALTER SEQUENCE seq_govio_templates RESTART WITH 2;	

-- INSERT INTO govio_service_instances(id,id_govhub_service,id_govhub_organization,id_govio_template,apikey) VALUES (1,1,1,1,'17886617e07d47e8b1ba314f2f1e3052');
-- ALTER SEQUENCE seq_govio_service_instances RESTART WITH 2;

-- INSERT INTO govio_placeholders(id,name,type,example) VALUES (1,'appointment','DATE','2100-12-31T12:00');
-- INSERT INTO govio_placeholders(id,name,type,example) VALUES (2,'at','STRING','Ufficio numero 1');
-- ALTER SEQUENCE seq_govio_placeholders RESTART WITH 3;

-- INSERT INTO govio_template_placeholders(id_govio_template,id_govio_placeholder, mandatory, index) VALUES ('1','1', true, 1);
-- INSERT INTO govio_template_placeholders(id_govio_template,id_govio_placeholder, mandatory, index) VALUES ('1','2', true, 2);