-- PATCH  03-03-2023 - RIMOZIONE TABELLA GOVIO_SERVICES

ALTER TABLE govio_service_instances ADD column id_govhub_service bigint;

UPDATE govio_service_instances SET id_govhub_service=gs.id_govhub_service FROM govio_services gs WHERE gs.id=id_govio_service;
ALTER TABLE govio_service_instances ALTER column id_govhub_service set not null;
ALTER TABLE govio_service_instances ADD CONSTRAINT fk_govio_srvinst_srv FOREIGN KEY (id_govhub_service) REFERENCES govhub_services (id);

update govio_service_instances SET id_govio_template=gs.id_govio_template FROM govio_services gs WHERE gs.id=id_govio_service AND id_govio_template=null;

alter table govio_service_instances drop constraint fk_govio_srvinst_srv;
drop table govio_services;

