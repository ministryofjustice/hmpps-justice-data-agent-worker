CREATE TABLE if not exists prompt
(
    id           UUID NOT NULL,
    prompt_key   VARCHAR(255),
    description  TEXT,
    is_deleted   BOOLEAN NOT NULL,
    created_by   UUID NOT NULL ,
    created_date TIMESTAMP NOT NULL ,
    CONSTRAINT pk_prompt PRIMARY KEY (id)
);

ALTER TABLE prompt
    ADD CONSTRAINT uc_prompt_promptkey UNIQUE (prompt_key);

