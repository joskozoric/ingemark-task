# product-app:

To run the project locally, you need the following installed:

Java 17+

Maven 3.6+

Docker & Docker Compose

## Basic setup
In the IDE of your choice (preferably Intellij) run the maven command on the top level pom.xml (product-app)
```
mvn install
```
In the Intellij run configurations define a new Spring configuration with the application main class:
```
com.ingemark.product_app.ProductAppApplication
```

### Run without docker
There is the option to run the application with an integrated H2 database and basic authentication (so no additional service are required)

In the run configuration set the spring profile to ```local-no-docker```

### Run with docker
This option requires running the additional services (postgres database and keycloak) via docker.

#### Docker setup
Firstly, set up the services by running the docker compose command in the ```/docker``` folder of the repository:
```
docker compose up -d
```

#### Keycloak user setup
Upon successful startup go to the keycloak admin console on ```http://localhost:8080``` and log in with the following credentials:
```
username: keycloak
password: keycloak
```
Afterward navigate to the user menu of the ```product``` realm:
```
Manage realms -> Select "product" realm -> Users (left sidebar) -> Add user
```
**Apart from the username, other fields (email, first name, last name) also need to be populated.**

Afterward in the credential tab click on the "Set password" option. Toggle the "Temporary" to off before saving.

This newly created user will be used for swagger testing.

In the run configuration set the spring profile to ```local-with-docker```

## Testing the API with swagger-ui
When the application is up and running, in your browser navigate to:
```
http://localhost:7777
```
You will be automatically redirected to the swagger-ui page.

### Authentication
#### Basic auth (no docker)
Click on the "lock" button and in the prompt under the "BasicAuth  (http, Basic)" enter credentials:
```
username: user
password: password
```
Confirm by clicking authorize. You can proceed to test the endpoints.

#### oAuth implicit flow (with docker)
Click on the "lock" button and in the prompt under the "oAuthPassword (OAuth2, implicit)" enter:
```
cliend_id: product_app
```
Select scopes at will via the checkboxes:
```
product_app_api:product.read -> grants read rights
product_app_api:product.write -> grants write rights
```

Confirm by clicking authorize. You will be redirected to keycloak where you can authenticate using the credentials of the user you created in keycloak.
Upon successful authentication, you can proceed to test the endpoints
