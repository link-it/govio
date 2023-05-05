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

-- PATCH 08-03-2023 - Aggiunta vincolo chiave univoca tax_code e legal_name

alter table govhub_organizations add constraint govhub_organizations_legal_name unique(legal_name);
alter table govhub_organizations add constraint govhub_organizations_tax_code unique(tax_code);


-- PATCH 08-03-2023 - Renaming foreign key govio

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

-- PATCH 16-03-2023 Vincolo univocità nome servizio
alter table govhub_services add constraint govhub_services_name unique(name);


-- PATCH 24-03-2023 No position duplicata per placeholders

alter table govio_template_placeholders 
       add constraint UniqueTemplatePlaceholderPosition unique (id_govio_template, position);


-- PATCH 24-03-2023 Template Name not Null

UPDATE govio_templates SET name = 'Template Demo' WHERE name IS NULL;

ALTER TABLE govio_templates ALTER COLUMN name SET NOT NULL;

-- PATCH 27-03-2023 Template Description TEXT

ALTER TABLE govio_templates ALTER COLUMN description TYPE TEXT;
ALTER TABLE govio_templates ALTER COLUMN message_body TYPE TEXT;


-- PATCH 28-03-2023 Placeholder Description TEXT

ALTER TABLE govio_placeholders ALTER COLUMN description TYPE TEXT;

UPDATE govhub_users SET full_name = 'Utente Demo' WHERE full_name IS NULL;
ALTER TABLE govhub_users ALTER column full_name SET NOT NULL;

-- PATCH 7-04-2023 Size not null per govio file

ALTER TABLE govio_files ALTER COLUMN size SET NOT NULL;


-- PATCH 19-04*2023 Resi unique file e line-number per i file-messages, in questo modo il batch non può
-- creare entry duplicate per una stessa riga di un dato csv

alter table govio_file_messages add constraint UniqueGovioFileLineNumber unique (id_govio_file, line_number);

-- PATCH 26-04-2023 Aggiunta campo io_service_id alle service instances

alter table govio_service_instances add column io_service_id varchar(255);
update govio_service_instances set io_service_id = 'IO SERVICE ID MANCANTE' where io_service_id is null;
alter table govio_service_instances alter column io_service_id set not null;

-- PATCH 27-04-2023 Aggiunta Idempotency keys per govio_messages

create table govio_messages_idempotency_keys (
	govio_message_id BIGINT not null,
	bean_hashcode BIGINT,
	idempotency_key uuid,
	primary key (govio_message_id)
);

alter table govio_messages_idempotency_keys 
   add constraint UniqueIdempotencykeyHashcode unique (idempotency_key, bean_hashcode);

alter table govio_messages_idempotency_keys 
   add constraint IdempotencyKey_GovioMessage 
   foreign key (govio_message_id) 
   references govio_messages;

create index BeanHashcodeIdx on govio_messages_idempotency_keys (bean_hashcode);

-- PATCH 28-04-2023 Reso unique il nome del placeholder

alter table govio_placeholders add constraint UniqueGovioPlaceholderName unique (name);

