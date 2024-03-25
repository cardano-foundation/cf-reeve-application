INSERT INTO organisation (organisation_id, name, tax_id_number, country_code, currency_id, accounting_period_months, pre_approve_transactions, pre_approve_transactions_dispatch, created_by, updated_by, created_at, updated_at) VALUES ('75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94', 'Cardano Foundation', 'CHE-184477354', 'CH', 'ISO_4217:CHF', 36, 'true', 'true', 'system',  'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


INSERT INTO lob_service.accounting_core_transaction_batch
(transaction_batch_id, status, filtering_parameters_organisation_id,filtering_parameters_from_date,filtering_parameters_to_date,filtering_parameters_accounting_period_from,filtering_parameters_accounting_period_to,created_at, updated_at)
VALUES
('eb47142027c0788116d14723a4ab4a67636a7d6463d84f0c6f7adf61aba32c04','CREATED','75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94','2014-04-18','2014-04-18','2021-04','2024-04', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

