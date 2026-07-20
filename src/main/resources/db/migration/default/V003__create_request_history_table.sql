CREATE TABLE if not exists request_history
(
    id                  UUID    NOT NULL,
    synchronous_request BOOLEAN NOT NULL,
    correlation_id      UUID,
    prompt_version_id   UUID,
    queued_at           TIMESTAMP WITHOUT TIME ZONE,
    received_at         TIMESTAMP WITHOUT TIME ZONE,
    completed_at        TIMESTAMP WITHOUT TIME ZONE,
    status              VARCHAR(255),
    error               VARCHAR(255),
    error_at            TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_requesthistory PRIMARY KEY (id)
);