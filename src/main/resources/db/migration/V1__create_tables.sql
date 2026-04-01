-- ------------------------------------------------------------------------
-- Schema: users, trips (viagens) and compensations (compensações)
-- ------------------------------------------------------------------------

-- 1) Usuários
CREATE TABLE USUARIOS (
    ID                       NUMBER(19,0)       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    NOME                     VARCHAR2(255 CHAR) NOT NULL,
    EMAIL                    VARCHAR2(255 CHAR) NOT NULL UNIQUE,
    SENHA                    VARCHAR2(255 CHAR) NOT NULL,
    ENABLED                  NUMBER(1)          DEFAULT 1    NOT NULL,
    FAILED_LOGIN_ATTEMPTS    NUMBER(2)          DEFAULT 0    NOT NULL,
    ACCOUNT_LOCKED_UNTIL     TIMESTAMP                     NULL,
    ACCOUNT_EXPIRATION_DATE  DATE                          NULL,
    ROLES                    VARCHAR2(255 CHAR)            NULL
);

-- 2) Viagens
CREATE TABLE VIAGENS (
    ID            NUMBER(19,0)       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    USUARIO_ID    NUMBER(19,0)       NOT NULL,
    TRANSPORTE    VARCHAR2(100 CHAR),
    DISTANCIA_KM  NUMBER(10,2),
    CO2_EMITIDO   NUMBER(10,2),
    DATA_HORA     TIMESTAMP          NOT NULL,
    CONSTRAINT FK_VIAGENS_USUARIO FOREIGN KEY (USUARIO_ID)
        REFERENCES USUARIOS(ID)
);

-- 3) Compensações
CREATE TABLE COMPENSACOES (
    ID                         NUMBER(19,0)       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    USUARIO_ID                 NUMBER(19,0)       NOT NULL,
    TIPO                       VARCHAR2(100 CHAR),
    QUANTIDADE                 NUMBER(10,2),
    DATA_REGISTRO              TIMESTAMP          NOT NULL,
    CONSTRAINT FK_COMPENS_USUARIO FOREIGN KEY (USUARIO_ID)
        REFERENCES USUARIOS(ID)
);
