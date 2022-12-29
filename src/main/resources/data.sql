INSERT INTO public.govhub_applications (id, application_id, deployed_uri, name) VALUES (nextval('public.seq_govhub_applications'), 'govio', 'http://localhost:8083/govio-app', 'GovIO');	
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govio'), 'govio_sender');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govio'), 'govio_viewer');