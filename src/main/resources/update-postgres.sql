-- PATCH  03-03-2023 - RIMOZIONE TABELLA GOVIO_SERVICES

ALTER TABLE govio_service_instances ADD column id_govhub_service bigint;

UPDATE govio_service_instances SET id_govhub_service=gs.id_govhub_service FROM govio_services gs WHERE gs.id=id_govio_service;

ALTER TABLE govio_service_instances ALTER column id_govhub_service set not null;

alter table govio_service_instances drop constraint fk_govio_srvinst_srv;

ALTER TABLE govio_service_instances ADD CONSTRAINT fk_govio_srvinst_srv FOREIGN KEY (id_govhub_service) REFERENCES govhub_services (id);

update govio_service_instances SET id_govio_template=gs.id_govio_template FROM govio_services gs WHERE gs.id=id_govio_service AND id_govio_template=null;


drop table govio_services;


-- PATCH 06-03-2023 - Vincoli

alter table govio_service_instances ALTER column id_govio_template set not null;

alter table govio_service_instances add constraint UniqueServiceOrganizationTemplate unique (id_govhub_service, id_govio_template, id_govhub_organization);


-- PATCH 06-03-2023 - Aggiunta campo 'enabled' per disabilitare le service instances

alter table govio_service_instances add column enabled boolean not null default true;


