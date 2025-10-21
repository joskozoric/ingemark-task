FROM quay.io/keycloak/keycloak:latest

ENV KC_HOME /opt/keycloak
ENV KC_IMPORT_DIR ${KC_HOME}/data/import

COPY product-realm.json ${KC_IMPORT_DIR}/

ENV KEYCLOAK_ADMIN=keycloak
ENV KEYCLOAK_ADMIN_PASSWORD=keycloak

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev", "--import-realm"]
