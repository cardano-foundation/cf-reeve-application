CREATE TABLE accounting_core_transaction_batch (
   transaction_batch_id CHAR(64) NOT NULL,

--   organisation_id CHAR(64) NOT NULL,

   status VARCHAR(255) NOT NULL,

   stats_total_transactions_count INT,
   stats_processed_transactions_count INT,
   stats_failed_transactions_count INT,
   stats_approved_transactions_count INT,
   stats_approved_transactions_dispatch_count INT,
   stats_dispatched_transactions_count INT,
   stats_completed_transactions_count INT,
   stats_finalized_transactions_count INT,
   stats_failed_source_erp_transactions_count INT,
   stats_failed_source_lob_transactions_count INT,

   filtering_parameters_organisation_id VARCHAR(255) NOT NULL,
   filtering_parameters_transaction_types SMALLINT,
   filtering_parameters_from_date DATE NOT NULL,
   filtering_parameters_to_date DATE NOT NULL,
   filtering_parameters_transaction_number VARCHAR(255),
   filtering_parameters_accounting_period_from CHAR(7) NOT NULL,
   filtering_parameters_accounting_period_to CHAR(7) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   PRIMARY KEY (transaction_batch_id)
);

CREATE TABLE accounting_core_transaction (
   transaction_id CHAR(64) NOT NULL,
   transaction_type VARCHAR(255) NOT NULL,
   batch_id CHAR(64) NOT NULL,

   FOREIGN KEY (batch_id) REFERENCES accounting_core_transaction_batch (transaction_batch_id),

   entry_date DATE NOT NULL,
   accounting_period CHAR(7) NOT NULL,
   transaction_internal_number VARCHAR(255) NOT NULL,
   fx_rate DECIMAL NOT NULL,

   organisation_id CHAR(64) NOT NULL,
   organisation_short_name VARCHAR(50),
   --organisation_currency_customer_code VARCHAR(255),
   organisation_currency_id VARCHAR(255),

   user_comment VARCHAR(255),
   rejection_status VARCHAR(255) NOT NULL,

   validation_status VARCHAR(255) NOT NULL,

   transaction_approved BOOLEAN NOT NULL,
   ledger_dispatch_approved BOOLEAN NOT NULL,
   ledger_dispatch_status VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   PRIMARY KEY (transaction_id)
);

CREATE TABLE accounting_core_transaction_violation (
   transaction_id CHAR(64) NOT NULL,
   tx_item_id CHAR(64) NOT NULL,
   code VARCHAR(255) NOT NULL,

   type VARCHAR(255) NOT NULL,
   sub_code VARCHAR(255) NOT NULL,
   source VARCHAR(255) NOT NULL,
   processor_module VARCHAR(255) NOT NULL,
   bag jsonb NOT NULL,

   CONSTRAINT fk_accounting_core_transaction_violation_id FOREIGN KEY (transaction_id) REFERENCES accounting_core_transaction (transaction_id),

   PRIMARY KEY (transaction_id, tx_item_id, code, sub_code)
);

CREATE TABLE accounting_core_transaction_filtering_params_transaction_number (
   owner_id CHAR(64) NOT NULL,
   transaction_number VARCHAR(255) NOT NULL,

   PRIMARY KEY (owner_id, transaction_number)
);

CREATE TABLE accounting_core_transaction_batch_assoc (
   transaction_batch_id CHAR(64) NOT NULL,
   transaction_id CHAR(64) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   FOREIGN KEY (transaction_batch_id) REFERENCES accounting_core_transaction_batch (transaction_batch_id),
   FOREIGN KEY (transaction_id) REFERENCES accounting_core_transaction (transaction_id),

   PRIMARY KEY (transaction_batch_id, transaction_id)
);


CREATE TABLE accounting_core_transaction_item (
   transaction_item_id CHAR(64) NOT NULL,

   transaction_id CHAR(64) NOT NULL,

   FOREIGN KEY (transaction_id) REFERENCES accounting_core_transaction (transaction_id),

   account_code_debit VARCHAR(255),
   account_code_ref_credit VARCHAR(255),

   account_code_credit VARCHAR(255),
   account_code_ref_debit VARCHAR(255),

   account_name_debit VARCHAR(255),
   account_event_code VARCHAR(255),

   amount_fcy DECIMAL NOT NULL,
   amount_lcy DECIMAL NOT NULL,

   document_num VARCHAR(255),
   document_currency_customer_code VARCHAR(255),
   document_currency_id VARCHAR(255),

   document_vat_customer_code VARCHAR(255),
   document_vat_rate DECIMAL,

   document_counterparty_customer_code VARCHAR(255),
   document_counterparty_type VARCHAR(255),
   document_counterparty_name VARCHAR(255),

   project_customer_code VARCHAR(255),

   cost_center_customer_code VARCHAR(255),
   cost_center_external_customer_code VARCHAR(255),
   cost_center_name VARCHAR(255),

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
