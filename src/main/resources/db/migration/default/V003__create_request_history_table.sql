CREATE TABLE if not exists request_history
(
    id                  UUID    NOT NULL,
    synchronous_request BOOLEAN NOT NULL,
    correlation_id      UUID,
    prompt_version_id   UUID,
    queued_at           TIMESTAMP,
    received_at         TIMESTAMP,
    completed_at        TIMESTAMP,
    status              VARCHAR(255),
    error               VARCHAR(255),
    error_at            TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_requesthistory PRIMARY KEY (id)
);
