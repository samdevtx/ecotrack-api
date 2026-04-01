-- Script de inicialização do banco de dados EcoTrack
-- Este script é executado automaticamente quando o container Oracle é iniciado

-- Conectar como SYSTEM para criar o usuário
CONNECT SYSTEM/Password123@@XEPDB1;

-- Criar tablespace para a aplicação (opcional)
CREATE TABLESPACE ECOTRACK_DATA
DATAFILE '/opt/oracle/oradata/XE/XEPDB1/ecotrack_data01.dbf'
SIZE 100M
AUTOEXTEND ON
NEXT 10M
MAXSIZE UNLIMITED;

-- Garantir que o usuário ecotrack existe e tem as permissões necessárias
CREATE USER ecotrack IDENTIFIED BY "Password123@"
DEFAULT TABLESPACE ECOTRACK_DATA
TEMPORARY TABLESPACE TEMP;

-- Conceder privilégios necessários
GRANT CONNECT, RESOURCE TO ecotrack;
GRANT CREATE SESSION TO ecotrack;
GRANT CREATE TABLE TO ecotrack;
GRANT CREATE SEQUENCE TO ecotrack;
GRANT CREATE VIEW TO ecotrack;
GRANT CREATE PROCEDURE TO ecotrack;
GRANT CREATE TRIGGER TO ecotrack;
GRANT UNLIMITED TABLESPACE TO ecotrack;

-- Privilégios adicionais para Flyway
GRANT SELECT ON DBA_OBJECTS TO ecotrack;
GRANT SELECT ON DBA_TAB_COLUMNS TO ecotrack;

-- Conectar como ecotrack para verificar a conexão
CONNECT ecotrack/"Password123@"@XEPDB1;

-- Criar uma tabela de teste para verificar se tudo está funcionando
CREATE TABLE test_connection (
    id NUMBER PRIMARY KEY,
    message VARCHAR2(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO test_connection (id, message) VALUES (1, 'Database initialized successfully');
COMMIT;

-- Mostrar informações sobre o usuário
SELECT USER, SYSDATE FROM DUAL;

EXIT;