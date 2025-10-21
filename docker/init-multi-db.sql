CREATE USER keycloak_user WITH PASSWORD 'password';
CREATE USER product_user WITH PASSWORD 'password';

CREATE DATABASE keycloakdb OWNER keycloak_user;
CREATE DATABASE productdb OWNER product_user;

-- keycloakdb
\c keycloakdb

GRANT ALL PRIVILEGES ON DATABASE keycloakdb TO keycloak_user;

ALTER SCHEMA public OWNER TO keycloak_user;

REVOKE ALL ON SCHEMA public FROM public;
GRANT ALL ON SCHEMA public TO keycloak_user;
GRANT USAGE ON SCHEMA public TO keycloak_user;

-- productdb
\c productdb

GRANT ALL PRIVILEGES ON DATABASE productdb TO product_user;

ALTER SCHEMA public OWNER TO product_user;

GRANT ALL ON SCHEMA public TO product_user;
GRANT USAGE ON SCHEMA public TO product_user;
