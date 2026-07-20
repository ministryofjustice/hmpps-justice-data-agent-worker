CREATE TABLE if not exists prompt
(
    id           UUID    NOT NULL,
    prompt_key   VARCHAR(255),
    description  VARCHAR(255),
    is_deleted   BOOLEAN NOT NULL,
    created_by   UUID,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_prompt PRIMARY KEY (id)
);