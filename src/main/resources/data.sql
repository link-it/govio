INSERT INTO public.govhub_applications (application_id, deployed_uri, name) VALUES (
	'govio', 'http://localhost:8083/govio-app', 'GovIO');	

INSERT INTO public.govhub_roles (id, id_govhub_application, name, description) VALUES (
	nextval('public.seq_govhub_roles'), 
	(SELECT id FROM govhub_applications WHERE application_id='govio'), 
	'govio_sender',
	'Autorizza all''invio e lettura messaggi di IO.');

INSERT INTO public.govhub_roles (id, id_govhub_application, name, description) VALUES (
	nextval('public.seq_govhub_roles'), 
	(SELECT id FROM govhub_applications WHERE application_id='govio'), 
	'govio_viewer'
	'Autorizza alla consultazione dei messaggi di IO.');

INSERT INTO public.govhub_roles (id, id_govhub_application, name, description) VALUES (
	nextval('public.seq_govhub_roles'),
	(SELECT id FROM govhub_applications WHERE application_id='govio'), 
	'govio_sysadmin',
	'Autorizza ad operare senza alcuna limitazione all''interno di GovIO');

INSERT INTO public.govhub_roles (id, id_govhub_application, name, description) VALUES (
	nextval('public.seq_govhub_roles'),
	(SELECT id FROM govhub_applications WHERE application_id='govio'), 
	'govio_service_viewer',
	'Autorizza alla consultazione di servizi e template.');

INSERT INTO public.govhub_roles (id, id_govhub_application, name, description) VALUES (
	nextval('public.seq_govhub_roles'), 
	(SELECT id FROM govhub_applications WHERE application_id='govio'), 
	'govio_service_editor',
	'Autorizza alla consultazione e modifica di servizi e template.');

---INSERT INTO public.govhub_roles (id, id_govhub_application, name, description) VALUES (
--	nextval('public.seq_govhub_roles'), 
--	(SELECT id FROM govhub_applications WHERE application_id='govio'), 
--	'govio_service_instance_viewer');

--INSERT INTO public.govhub_roles (id, id_govhub_application, name, description) VALUES (
--	nextval('public.seq_govhub_roles'), 
--	(SELECT id FROM govhub_applications WHERE application_id='govio'), 
--	'govio_service_instance_editor');
