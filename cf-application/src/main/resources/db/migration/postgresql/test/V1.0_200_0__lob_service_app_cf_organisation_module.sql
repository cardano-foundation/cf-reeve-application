INSERT INTO organisation_vat (organisation_id, customer_code, rate, created_by, updated_by, created_at, updated_at) VALUES ('dummy-organisation', 'UNDEF-CH', 0, 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO organisation_project (organisation_id, customer_code, external_customer_code, name, created_by, updated_by, created_at, updated_at) VALUES ('dummy-organisation', 'AN 000001 2023', 'AN 000001 2023', 'AN 000001', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO organisation_currency (organisation_id, customer_code, currency_id, created_by, updated_by, created_at, updated_at) VALUES ('dummy-organisation', 'CHF', 'ISO_4217:CHF', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO organisation_currency (organisation_id, customer_code, currency_id, created_by, updated_by, created_at, updated_at) VALUES ('dummy-organisation', 'ADA', 'ISO_24165:ADA:HWGL1C2CK', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO organisation_cost_center (organisation_id, customer_code, external_customer_code, name, created_by, updated_by, created_at, updated_at) VALUES ('dummy-organisation', '1000', '1000', 'Test', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO organisation_chart_of_account (organisation_id, customer_code, ref_code, event_ref_code, name, created_by, updated_by, created_at, updated_at) VALUES ('dummy-organisation', '0000000000', '0000', 'T0000', 'LOB Dummy Account', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO organisation_chart_of_account (organisation_id, customer_code, ref_code, event_ref_code, name, created_by, updated_by, created_at, updated_at) VALUES ('dummy-organisation', '1203168240', 'W231', 'W100', 'Delegated Address', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO organisation_account_event (organisation_id, customer_code, name, created_by, updated_by, created_at, updated_at) VALUES ('dummy-organisation', '11001100', 'Bank Transfer', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO organisation_account_event (organisation_id, customer_code, name, created_by, updated_by, created_at, updated_at) VALUES ('dummy-organisation', 'W100T0000', 'Crypto Transfer in', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO organisation_chart_of_account_type (id,organisation_id,name,created_by,updated_by,created_at,updated_at) VALUES (1,'dummy-organisation', 'ASSET','system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO organisation_chart_of_account_sub_type (id, organisation_id, name, type, created_by, updated_by, created_at, updated_at) VALUES (1,'dummy-organisation','CASH_AND_CASH_EQUIVALENTS',1,'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


