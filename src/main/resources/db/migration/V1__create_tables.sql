-- ------------------------------------------------------------------------
-- Schema: users, trips (viagens) and compensations (compensações)
-- ------------------------------------------------------------------------

-- 1) Usuários
CREATE TABLE USUARIOS (
    ID                       BIGINT             GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    NOME                     VARCHAR(255)       NOT NULL,
    EMAIL                    VARCHAR(255)       NOT NULL UNIQUE,
    SENHA                    VARCHAR(255)       NOT NULL,
    ENABLED                  BOOLEAN            DEFAULT TRUE NOT NULL,
    FAILED_LOGIN_ATTEMPTS    SMALLINT           DEFAULT 0    NOT NULL,
    ACCOUNT_LOCKED_UNTIL     TIMESTAMP                       NULL,
    ACCOUNT_EXPIRATION_DATE  DATE                            NULL,
    ROLES                    VARCHAR(255)                    NULL
);

-- 2) Viagens
CREATE TABLE VIAGENS (
    ID            BIGINT             GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    USUARIO_ID    BIGINT             NOT NULL,
    TRANSPORTE    VARCHAR(100),
    DISTANCIA_KM  NUMERIC(10,2),
    CO2_EMITIDO   NUMERIC(10,2),
    DATA_HORA     TIMESTAMP          NOT NULL,
    CONSTRAINT FK_VIAGENS_USUARIO FOREIGN KEY (USUARIO_ID)
        REFERENCES USUARIOS(ID)
);

-- 3) Compensações
CREATE TABLE COMPENSACOES (
    ID                         BIGINT             GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    USUARIO_ID                 BIGINT             NOT NULL,
    TIPO                       VARCHAR(100),
    QUANTIDADE                 NUMERIC(10,2),
    DATA_REGISTRO              TIMESTAMP          NOT NULL,
    CONSTRAINT FK_COMPENS_USUARIO FOREIGN KEY (USUARIO_ID)
        REFERENCES USUARIOS(ID)
);
