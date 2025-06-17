CREATE TABLE asset_code_count
(
    id         VARCHAR(255)      NOT NULL,
    last_value INTEGER DEFAULT 0 NOT NULL,
    version    BIGINT  DEFAULT 0 NOT NULL,
    CONSTRAINT pk_asset_code_count PRIMARY KEY (id)
);

CREATE TABLE assets
(
    id             UUID          NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE,
    updated_at     TIMESTAMP WITHOUT TIME ZONE,
    created_by     VARCHAR(255),
    updated_by     VARCHAR(255),
    code           VARCHAR(8)    NOT NULL,
    name           VARCHAR(255)   NOT NULL,
    specification  VARCHAR(2000) NOT NULL,
    installed_date date          NOT NULL,
    state          VARCHAR(20)   NOT NULL,
    category_id    INTEGER,
    location_id    UUID,
    CONSTRAINT pk_assets PRIMARY KEY (id)
);

ALTER TABLE assets
    ADD CONSTRAINT uc_assets_code UNIQUE (code);

ALTER TABLE assets
    ADD CONSTRAINT FK_ASSETS_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES categories (id);

ALTER TABLE assets
    ADD CONSTRAINT FK_ASSETS_ON_LOCATION FOREIGN KEY (location_id) REFERENCES locations (id);