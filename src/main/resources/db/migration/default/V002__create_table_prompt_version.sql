CREATE TABLE if not exists prompt_version
(
    id                UUID NOT NULL,
    version           INTEGER NOT NULL,
    prompt_id         UUID NOT NULL ,
    prompt_template   TEXT,
    request_contract  JSONB NOT NULL,
    response_contract JSONB,
    created_by        UUID NOT NULL ,
    created_date      TIMESTAMP NOT NULL ,
    CONSTRAINT pk_promptversion PRIMARY KEY (id)
);

ALTER TABLE prompt_version
    ADD CONSTRAINT FK_PROMPTVERSION_ON_PROMPT FOREIGN KEY (prompt_id) REFERENCES prompt (id);
