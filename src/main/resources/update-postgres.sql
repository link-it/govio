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

-- PATCH 08-03-2023 - Rimozione colonna id_govio_service, completa la prima patch

alter table govio_service_instances drop column id_govio_service;

-- PATCH 08-03-2020 - Aggiunta vincolo chiave univoca tax_code e legal_name

alter table govhub_organizations add constraint govhub_organizations_legal_name unique(legal_name);
alter table govhub_organizations add constraint govhub_organizations_tax_code unique(tax_code);


-- PATCH 08-03-2020 - Renaming foreign key govio

alter table govio_template_placeholders rename constraint fk_govio_tp_placeholder TO GovioTemplatePlaceholder_GovioPlaceholder;
alter table govio_template_placeholders rename constraint fk_govio_tp_template TO GovioTemplatePlaceholder_GovioTemplate;


alter table govio_service_instances rename constraint fk_govio_srvinst_template TO GovioServiceInstance_GovioTemplate;
alter table govio_service_instances rename constraint fk_govio_srvinst_srv TO GovioServiceInstance_GovhubService;
alter table govio_service_instances rename constraint fk_govio_srvinst_huborg TO GovioServiceInstance_GovhubOrganization;


alter table govio_files rename constraint fk_govio_files_srvinst TO GovioFiles_GovioServiceInstance;
alter table govio_files rename constraint fk_govio_files_hubuser TO GovioFiles_GovhubUser;


alter table govio_messages rename constraint fk_govio_msg_srvinst TO GovioMessages_GovioServiceInstance;
alter table govio_messages rename constraint fk_govio_msg_hubuser TO GovioMessages_GovhubUser;

alter table govio_file_messages rename constraint fk_govio_filemsg_file TO GovioFileMessagess_GovioFle;
alter table govio_file_messages rename constraint fk_govio_filemsg_msg TO GovioFileMessagess_GovioMessage;

