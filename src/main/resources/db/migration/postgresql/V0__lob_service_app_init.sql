-- Spring Data Envers
CREATE SEQUENCE IF NOT EXISTS revinfo_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE revinfo (
   rev BIGINT NOT NULL,
   rev_timestamp TIMESTAMP WITHOUT TIME ZONE,
   CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

--CREATE SEQUENCE  IF NOT EXISTS netsuite_ingestion_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE netsuite_ingestion (
   id UUID NOT NULL,
   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,
   ingestion_body TEXT NOT NULL,
   ingestion_body_checksum VARCHAR(255) NOT NULL,
   CONSTRAINT pk_netsuite_ingestion PRIMARY KEY (id)
);

CREATE TABLE accounting_core_transaction (
   transaction_id CHAR(64) NOT NULL,
   organisation_id VARCHAR(255) NOT NULL,
   organisation_currency_id VARCHAR(255) NOT NULL,
   organisation_currency_internal_number VARCHAR(255) NOT NULL,

   transaction_type VARCHAR(255) NOT NULL,
   entry_date DATE NOT NULL,
   transaction_internal_number VARCHAR(255) NOT NULL,
   fx_rate DECIMAL NOT NULL,

   document_internal_number VARCHAR(255),
   document_currency_id VARCHAR(255),
   document_currency_internal_number VARCHAR(255),

   document_vat_internal_number VARCHAR(255),
   document_vat_rate DECIMAL,

   document_counterparty_internal_number VARCHAR(255),
   document_counterparty_name VARCHAR(255),

   cost_center_internal_number VARCHAR(255),
   cost_center_external_number VARCHAR(255),
   cost_center_code VARCHAR(25),

   project_internal_number VARCHAR(255),
   project_code VARCHAR(25),

   validation_status VARCHAR(255) NOT NULL,

   ledger_dispatch_approved BOOLEAN NOT NULL,
   ledger_dispatch_status VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   PRIMARY KEY (transaction_id)
);

CREATE TABLE accounting_core_transaction_item (
   transaction_item_id CHAR(64) NOT NULL,

   transaction_id CHAR(64) NOT NULL,

   FOREIGN KEY (transaction_id) REFERENCES accounting_core_transaction (transaction_id),

   account_code_debit VARCHAR(255),
   account_name_debit VARCHAR(255),
   account_code_credit VARCHAR(255),

   amount_fcy DECIMAL NOT NULL,
   amount_lcy DECIMAL NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   PRIMARY KEY (transaction_item_id)
);

--CREATE TABLE accounting_core_transaction_line_aud (
--   id CHAR(64) NOT NULL,
--   organisation_id VARCHAR(255) NOT NULL,
--   transaction_type VARCHAR(255) NOT NULL,
--   entry_date DATE NOT NULL,
--   transaction_internal_number VARCHAR(255) NOT NULL,
--   base_currency_id VARCHAR(255),
--   base_currency_internal_code VARCHAR(255) NOT NULL,
--   target_currency_id VARCHAR(255),
--   target_currency_internal_code VARCHAR(255) NOT NULL,
--   fx_rate DECIMAL NOT NULL,
--   document_internal_number VARCHAR(255),
--   counterparty_internal_code VARCHAR(255),
--   counterparty_name VARCHAR(255),
--   cost_center_internal_code VARCHAR(255),
--   project_internal_code VARCHAR(255),
--   vat_internal_code VARCHAR(255),
--   vat_rate DECIMAL,
--   account_code_debit VARCHAR(255),
--   account_name_debit VARCHAR(255),
--   account_code_credit VARCHAR(255),
--   validation_status VARCHAR(255) NOT NULL,
--   ledger_dispatch_approved BOOLEAN NOT NULL,
--
--   amount_fcy DECIMAL NOT NULL,
--   amount_lcy DECIMAL NOT NULL,
--   ledger_dispatch_status VARCHAR(255) NOT NULL,
--
--   created_by VARCHAR(255),
--   updated_by VARCHAR(255),
--   created_at TIMESTAMP WITHOUT TIME ZONE,
--   updated_at TIMESTAMP WITHOUT TIME ZONE,
--
--   -- special for audit tables
--   rev INTEGER NOT NULL,
--   revtype SMALLINT,
--
--   CONSTRAINT acc_core_transaction_line_aud_pkey PRIMARY KEY (id, rev),
--   CONSTRAINT acc_core_transaction_line_aud_revinfo FOREIGN KEY (rev)
--   REFERENCES revinfo (rev) MATCH SIMPLE
--   ON UPDATE NO ACTION ON DELETE NO ACTION
--);

CREATE TABLE blockchain_publisher_transaction (
   transaction_id CHAR(64) NOT NULL,
   organisation_id VARCHAR(255) NOT NULL,
   internal_number VARCHAR(255) NOT NULL,

   organisation_currency_id VARCHAR(255) NOT NULL,
   --organisation_currency_internal_number VARCHAR(255) NOT NULL,

   transaction_type VARCHAR(255) NOT NULL,
   entry_date DATE NOT NULL,

   fx_rate DECIMAL NOT NULL,

   --cost_center_internal_number VARCHAR(255),
   cost_center_code VARCHAR(25),

   --project_internal_number VARCHAR(255),
   project_code VARCHAR(25),

   document_internal_document_number VARCHAR(255),
   document_currency_id VARCHAR(255) NOT NULL,
   -- document_currency_internal_number VARCHAR(255) NOT NULL,
   document_counterparty_internal_number VARCHAR(255),

   -- document_vat_internal_number VARCHAR(255),
   document_vat_rate DECIMAL,

   l1_assurance_level VARCHAR(255),
   l1_transaction_hash CHAR(64),
   l1_absolute_slot BIGINT,
   l1_creation_slot BIGINT,
   l1_publish_status VARCHAR(255),

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   PRIMARY KEY (transaction_id)
);

CREATE TABLE blockchain_publisher_transaction_item (
   transaction_item_id CHAR(64) NOT NULL,

   transaction_id CHAR(64) NOT NULL,

   FOREIGN KEY (transaction_id) REFERENCES blockchain_publisher_transaction (transaction_id),

   amount_fcy DECIMAL NOT NULL,

   event_code VARCHAR(255),

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   PRIMARY KEY (transaction_item_id)
);

--CREATE TABLE blockchain_publisher_transaction_line_aud (
--   id CHAR(64) NOT NULL,
--   organisation_id VARCHAR(255) NOT NULL,
--   upload_id UUID NOT NULL,
--   transaction_type VARCHAR(255) NOT NULL,
--   entry_date DATE NOT NULL,
--   transaction_internal_number VARCHAR(255),
--   base_currency_id VARCHAR(255),
--   base_currency_internal_code VARCHAR(255) NOT NULL,
--   target_currency_id VARCHAR(255),
--   target_currency_internal_code VARCHAR(255) NOT NULL,
--   fx_rate DECIMAL NOT NULL,
--   document_internal_number VARCHAR(255),
--   vendor_internal_code VARCHAR(255),
--   vat_internal_code VARCHAR(255),
--   vat_rate DECIMAL,
--   publish_status VARCHAR(255) NOT NULL,
--   l1_assurance_level VARCHAR(255),
--   l1_transaction_hash VARCHAR(255),
--   l1_absolute_slot BIGINT,
--
--   amount_fcy DECIMAL NOT NULL,
--   amount_lcy DECIMAL NOT NULL,
--
--   created_by VARCHAR(255),
--   updated_by VARCHAR(255),
--   created_at TIMESTAMP WITHOUT TIME ZONE,
--   updated_at TIMESTAMP WITHOUT TIME ZONE,
--
--   -- special for audit tables
--   rev INTEGER NOT NULL,
--   revtype SMALLINT,
--
--   CONSTRAINT blockchain_transaction_line_aud_pkey PRIMARY KEY (id, rev),
--   CONSTRAINT blockchain_transaction_line_aud_revinfo FOREIGN KEY (rev)
--   REFERENCES revinfo (rev) MATCH SIMPLE
--   ON UPDATE NO ACTION ON DELETE NO ACTION
--);

-- Spring Modulith
CREATE TABLE IF NOT EXISTS event_publication(
  id               UUID NOT NULL,
  listener_id      TEXT NOT NULL,
  event_type       TEXT NOT NULL,
  serialized_event TEXT NOT NULL,
  publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
  completion_date  TIMESTAMP WITH TIME ZONE,
  PRIMARY KEY (id)
);

--CREATE INDEX IF NOT EXISTS event_publication_listener_id_serialized_event_idx ON event_publication (listener_id, serialized_event);
CREATE INDEX IF NOT EXISTS event_publication_completion_date_idx ON event_publication (completion_date);