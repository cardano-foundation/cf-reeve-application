DROP TABLE IF EXISTS netsuite_ingestion;

CREATE TABLE netsuite_ingestion (
    id VARCHAR(255) NOT NULL,
    version INTEGER NOT NULL,

    ingestion_body TEXT NOT NULL,
    ingestion_body_checksum VARCHAR(255) NOT NULL,

    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,

   CONSTRAINT netsuite_ingestion_id PRIMARY KEY (id)
);
