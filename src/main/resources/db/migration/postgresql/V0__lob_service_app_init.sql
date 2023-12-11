CREATE TABLE IF NOT EXISTS netsuite_ingestion (
    id VARCHAR(255) NOT NULL,
    version INTEGER NOT NULL,

    ingestion_body TEXT NOT NULL,
    ingestion_body_checksum VARCHAR(255) NOT NULL,

    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,

   CONSTRAINT netsuite_ingestion_id PRIMARY KEY (id)
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
