CREATE TABLE if not exists prompt_version
(
    id                UUID    NOT NULL,
    version           INTEGER NOT NULL,
    prompt_id         UUID,
    prompt_template   TEXT,
    request_contract  JSONB,
    response_contract JSONB,
    created_by        UUID,
    created_date      TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_promptversion PRIMARY KEY (id)
);

ALTER TABLE prompt_version
    ADD CONSTRAINT FK_PROMPTVERSION_ON_PROMPT FOREIGN KEY (prompt_id) REFERENCES prompt (id);
