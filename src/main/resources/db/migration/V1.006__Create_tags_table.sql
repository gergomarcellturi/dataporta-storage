CREATE TABLE IF NOT EXISTS TAGS(
     UID VARCHAR(100)  NOT NULL DEFAULT public.uuid_generate_v4(),
     TITLE VARCHAR NOT NULL,
     CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     LAST_MODIFIED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     CONSTRAINT PK_TAG PRIMARY KEY (UID)
);
