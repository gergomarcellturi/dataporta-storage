CREATE TABLE IF NOT EXISTS METADATA(
    UID VARCHAR(100)  NOT NULL DEFAULT public.uuid_generate_v4(),
    USER_UID VARCHAR NOT NULL,
    FILENAME TEXT NOT NULL,
    SIZE BIGINT NOT NULL,
    TYPE TEXT NOT NULL,
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT PK_METADATA PRIMARY KEY (UID)
    );
