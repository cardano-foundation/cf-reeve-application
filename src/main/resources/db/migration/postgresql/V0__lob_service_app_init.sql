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

--CREATE TABLE netsuite_ingestion_audit (
--   id UUID NOT NULL,
--   revision INTEGER NOT NULL,
--   revision_type SMALLINT,
--   ingestion_body TEXT,
--   ingestion_body_checksum VARCHAR(255)
--);

CREATE TABLE accounting_core_transaction_line (
   id CHAR(64) NOT NULL,
   organisation_id VARCHAR(255) NOT NULL,
   transaction_type VARCHAR(255) NOT NULL,
   ingestion_id UUID NOT NULL,
   entry_date DATE NOT NULL,
   transaction_internal_number VARCHAR(255),
   base_currency_id VARCHAR(255),
   base_currency_internal_code VARCHAR(255) NOT NULL,
   target_currency_id VARCHAR(255),
   target_currency_internal_code VARCHAR(255) NOT NULL,
   fx_rate DECIMAL NOT NULL,
   document_internal_number VARCHAR(255),
   vendor_internal_code VARCHAR(255),
   vendor_name VARCHAR(255),
   cost_center_internal_code VARCHAR(255),
   project_internal_code VARCHAR(255),
   vat_internal_code VARCHAR(255),
   vat_rate DECIMAL,
   account_code_debit VARCHAR(255),
   account_name_debit VARCHAR(255),
   account_code_credit VARCHAR(255),
   validation_status VARCHAR(255) NOT NULL,
   ledger_dispatch_approved BOOLEAN NOT NULL,

   amount_fcy DECIMAL NOT NULL,
   amount_lcy DECIMAL NOT NULL,
   ledger_dispatch_status VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   CONSTRAINT pk_accounting_core_transaction_line PRIMARY KEY (id)
);

CREATE TABLE accounting_core_transaction_line_audit (
   id CHAR(64) NOT NULL,
   organisation_id VARCHAR(255) NOT NULL,
   transaction_type VARCHAR(255) NOT NULL,
   ingestion_id UUID NOT NULL,
   entry_date DATE NOT NULL,
   transaction_internal_number VARCHAR(255),
   base_currency_id VARCHAR(255),
   base_currency_internal_code VARCHAR(255) NOT NULL,
   target_currency_id VARCHAR(255),
   target_currency_internal_code VARCHAR(255) NOT NULL,
   fx_rate DECIMAL NOT NULL,
   document_internal_number VARCHAR(255),
   vendor_internal_code VARCHAR(255),
   vendor_name VARCHAR(255),
   cost_center_internal_code VARCHAR(255),
   project_internal_code VARCHAR(255),
   vat_internal_code VARCHAR(255),
   vat_rate DECIMAL,
   account_code_debit VARCHAR(255),
   account_name_debit VARCHAR(255),
   account_code_credit VARCHAR(255),
   validation_status VARCHAR(255) NOT NULL,
   ledger_dispatch_approved BOOLEAN NOT NULL,

   amount_fcy DECIMAL NOT NULL,
   amount_lcy DECIMAL NOT NULL,
   ledger_dispatch_status VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   -- special for audit tables
   revision INTEGER NOT NULL,
   revision_type SMALLINT

   --CONSTRAINT pk_accounting_core_transaction_line_audit PRIMARY KEY (id)
);

CREATE TABLE blockchain_publisher_transaction_line (
   id CHAR(64) NOT NULL,
   organisation_id VARCHAR(255) NOT NULL,
   upload_id UUID NOT NULL,
   transaction_type VARCHAR(255) NOT NULL,
   entry_date DATE NOT NULL,
   transaction_internal_number VARCHAR(255),
   base_currency_id VARCHAR(255),
   base_currency_internal_code VARCHAR(255) NOT NULL,
   target_currency_id VARCHAR(255),
   target_currency_internal_code VARCHAR(255) NOT NULL,
   fx_rate DECIMAL NOT NULL,
   document_internal_number VARCHAR(255),
   vendor_internal_code VARCHAR(255),
   vat_internal_code VARCHAR(255),
   vat_rate DECIMAL,
   publish_status VARCHAR(255) NOT NULL,
   l1_assurance_level VARCHAR(255),
   l1_transaction_hash VARCHAR(255),
   l1_absolute_slot BIGINT,

   amount_fcy DECIMAL NOT NULL,
   amount_lcy DECIMAL NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   CONSTRAINT pk_blockchain_publisher_transaction_line PRIMARY KEY (id)
);

CREATE TABLE blockchain_publisher_transaction_line_audit (
   id CHAR(64) NOT NULL,
   organisation_id VARCHAR(255) NOT NULL,
   upload_id UUID NOT NULL,
   transaction_type VARCHAR(255) NOT NULL,
   entry_date DATE NOT NULL,
   transaction_internal_number VARCHAR(255),
   base_currency_id VARCHAR(255),
   base_currency_internal_code VARCHAR(255) NOT NULL,
   target_currency_id VARCHAR(255),
   target_currency_internal_code VARCHAR(255) NOT NULL,
   fx_rate DECIMAL NOT NULL,
   document_internal_number VARCHAR(255),
   vendor_internal_code VARCHAR(255),
   vat_internal_code VARCHAR(255),
   vat_rate DECIMAL,
   publish_status VARCHAR(255) NOT NULL,
   l1_assurance_level VARCHAR(255),
   l1_transaction_hash VARCHAR(255),
   l1_absolute_slot BIGINT,

   amount_fcy DECIMAL NOT NULL,
   amount_lcy DECIMAL NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   -- special for audit tables
   revision INTEGER NOT NULL,
   revision_type SMALLINT

   --CONSTRAINT pk_blockchain_publisher_transaction_line_audit PRIMARY KEY (id)
);

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

-- Spring Data Envers
CREATE SEQUENCE IF NOT EXISTS revinfo_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE revinfo (
   rev BIGINT NOT NULL,
   rev_timestamp TIMESTAMP WITHOUT TIME ZONE,
   CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);
