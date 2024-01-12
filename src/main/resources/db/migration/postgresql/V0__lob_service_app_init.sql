CREATE SEQUENCE  IF NOT EXISTS netsuite_ingestion_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE netsuite_ingestion (
   id BIGINT NOT NULL,
   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,
   ingestion_body TEXT NOT NULL,
   ingestion_body_checksum VARCHAR(255) NOT NULL,
   CONSTRAINT pk_netsuite_ingestion PRIMARY KEY (id)
);

CREATE TABLE netsuite_ingestion_audit (
   id BIGINT NOT NULL,
   revision INTEGER NOT NULL,
   revision_type SMALLINT,
   ingestion_body TEXT,
   ingestion_body_checksum VARCHAR(255)
);

CREATE TABLE accounting_core_transaction_line (
  id UUID NOT NULL,
   organisation_id VARCHAR(255),
   transaction_type VARCHAR(255),
   entry_date TIMESTAMP WITHOUT TIME ZONE,
   transaction_number VARCHAR(255),
   account_code_debit VARCHAR(255),
   base_currency VARCHAR(4),
   currency VARCHAR(4),
   fx_rate DECIMAL,
   document_number VARCHAR(255),
   vendor_code VARCHAR(255),
   vendor_name VARCHAR(255),
   cost_center VARCHAR(255),
   project_code VARCHAR(255),
   vat_code VARCHAR(255),
   account_name_debit VARCHAR(255),
   account_credit VARCHAR(255),
   memo VARCHAR(255),
   amount_fcy DECIMAL,
   amount_lcy DECIMAL,
   CONSTRAINT pk_transaction_line PRIMARY KEY (id)
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
