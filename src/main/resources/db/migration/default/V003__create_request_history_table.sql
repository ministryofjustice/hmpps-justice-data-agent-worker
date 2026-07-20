CREATE TABLE if not exists request_history
(
    id                  UUID NOT NULL,
    synchronous_request BOOLEAN NOT NULL,
    correlation_id      UUID NOT NULL ,
    prompt_version_id   UUID NOT NULL ,
    queued_at           TIMESTAMP,
    received_at         TIMESTAMP,
    completed_at        TIMESTAMP,
    status              VARCHAR(255),
    constraint request_history_status_check
    check ((status)::text = ANY
    ((ARRAY
    [
        'QUEUED'::character varying,
        'PROCESSING'::character varying,
        'SUCCEEDED'::character varying,
        'FAILED'::character varying,
        'REJECTED'::character varying
    ])::text[])),
    error               TEXT,
    error_at            TIMESTAMP,
    CONSTRAINT pk_requesthistory PRIMARY KEY (id)
);
