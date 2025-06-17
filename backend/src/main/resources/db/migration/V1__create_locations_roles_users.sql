create TABLE locations
(
    id   UUID         NOT NULL,
    code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_locations PRIMARY KEY (id)
);

create TABLE roles
(
    id          UUID        NOT NULL,
    name        VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

create TABLE user_roles
(
    role_id UUID NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (role_id, user_id)
);

create TABLE users
(
    id              UUID         NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE,
    updated_at      TIMESTAMP WITHOUT TIME ZONE,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    staff_code      VARCHAR(10)  NOT NULL,
    username        VARCHAR(50)  NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    first_name      VARCHAR(128) NOT NULL,
    last_name       VARCHAR(128) NOT NULL,
    date_of_birth   date         NOT NULL,
    joined_on       date         NOT NULL,
    gender          VARCHAR(255),
    location_id     UUID,
    status          VARCHAR(255),
    CONSTRAINT pk_users PRIMARY KEY (id)
);

alter table locations
    add CONSTRAINT uc_locations_code UNIQUE (code);

alter table roles
    add CONSTRAINT uc_roles_name UNIQUE (name);

alter table users
    add CONSTRAINT uc_users_hashedpassword UNIQUE (hashed_password);

alter table users
    add CONSTRAINT uc_users_staffcode UNIQUE (staff_code);

alter table users
    add CONSTRAINT uc_users_username UNIQUE (username);

alter table users
    add CONSTRAINT FK_USERS_ON_LOCATION FOREIGN KEY (location_id) REFERENCES locations (id);

alter table user_roles
    add CONSTRAINT fk_userol_on_role FOREIGN KEY (role_id) REFERENCES roles (id);

alter table user_roles
    add CONSTRAINT fk_userol_on_user FOREIGN KEY (user_id) REFERENCES users (id);