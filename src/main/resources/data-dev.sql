-- Utenti

INSERT INTO govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'user_govio_sender', 'Marco Sender', 'govio_sender@govio.it', true);
INSERT INTO govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'user_govio_viewer', 'Angovio Visore', 'govio_viewer@govio.it', true);
INSERT INTO govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'govio_sysadmin', 'Giovadmin', 'sysadmin@govio.it', true);

-- Ruoli


INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 2, 'govio_sender');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 2, 'govio_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 2, 'govio_sysadmin');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 2, 'govio_service_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 2, 'govio_service_editor');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 2, 'govio_service_instance_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 2, 'govio_service_instance_editor');

-- amministratore -> govio_sysadmin

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='amministratore'), (SELECT id FROM public.govhub_roles WHERE name='govio_sysadmin'));


-- govio_sysadmin -> govio_sysadmin

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='govio_sysadmin'), (SELECT id FROM public.govhub_roles WHERE name='govio_sysadmin'));


-- user_govio_sender -> govio_sender

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='user_govio_sender'), (SELECT id FROM public.govhub_roles WHERE name='govio_sender'));


-- user_govio_viewer -> govio_viewer

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='user_govio_viewer'), (SELECT id FROM public.govhub_roles WHERE name='govio_viewer'));

-- Creo un template dummy

INSERT INTO public.govio_templates (id) VALUES (nextval('public.seq_govio_templates'));

-- Creo un govio_service

INSERT INTO public.govio_services (id, id_govio_template, id_govhub_service) VALUES(nextval('public.seq_govio_services'), 1, 1);

-- Creo anche una service instance

INSERT INTO public.govio_service_instances(id, id_govio_service, id_govhub_organization, id_govio_template) VALUES (nextval('public.seq_govio_service_instances'), 1, 1, 1);

-- Service instance senza template

INSERT INTO public.govio_service_instances(id, id_govio_service, id_govhub_organization, id_govio_template) VALUES (nextval('public.seq_govio_service_instances'), 1, 2, null);



-- inserimento dati per la configurazione di un template per la spedizione di un messaggio

INSERT INTO govio_templates(id, message_body, subject, has_due_date, has_payment) VALUES (nextval('public.seq_govio_templates'), 'Salve, con la presente la informiamo che in data ${due_date} scadrà la Carta di Identità elettronica numero ${cie.uppercase}. Per maggiori informazioni sulle modalità di rinnovo può consultare https://comune.dimostrativo.it.', 'Scadenza CIE n. ${cie.uppercase}', true, false);

INSERT INTO govio_services(id, id_govio_template, id_govhub_service) VALUES (nextval('public.seq_govio_services'), '2', (select id from public.govhub_services where name='CIE') );

INSERT INTO govio_service_instances(id, id_govio_service, id_govhub_organization, id_govio_template,apikey) VALUES (nextval('public.seq_govio_services_instances'), '2', (select id from public.govhub_organizations where taxcode='80015010723') , '2', '17886617e07d47e8b1ba314f2f1e3052');

INSERT INTO govio_placeholders(id, name, type, example) VALUES (nextval('public.seq_govio_placeholders'), 'cie', 'STRING', 'CA000000AA');

INSERT INTO govio_template_placeholders(id_govio_template, id_govio_placeholder, mandatory, index) VALUES (nextval('public.seq_govio_template_placeholders'), '1', true, 1);
