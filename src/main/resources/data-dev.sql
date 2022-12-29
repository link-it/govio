-- Utenti

INSERT INTO govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'user_govio_sender', 'Lorenzo Nardi', 'nardi@link.it', true);

-- Ruoli

WITH govhub_user AS (
    SELECT *
    FROM govhub_users
    WHERE principal = 'user_govio_sender'
);


INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 2, 'govio_sender');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 2, 'govio_viewer');


-- user_govio_sender -> govio_sender

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='user_govio_sender'), (SELECT id FROM public.govhub_roles WHERE name='govio_sender'));


-- user_govio_viewer -> govio_viewer

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='user_govio_viewer'), (SELECT id FROM public.govhub_roles WHERE name='govio_viewer'));

-- Creo un template dummy

INSERT INTO public.govio_templates (id) VALUES (nextval('public.seq_govio_templates'));


-- Creo anche una service instance

INSERT INTO public.govio_service_instances(id, id_govhub_service, id_govhub_organization, id_govio_template) VALUES (nextval('public.seq_govio_service_instances'), 1, 1, 1);

-- Service instance senza template

INSERT INTO public.govio_service_instances(id, id_govhub_service, id_govhub_organization, id_govio_template) VALUES (nextval('public.seq_govio_service_instances'), 1, 2, null);
