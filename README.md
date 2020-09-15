![Gradle tests](https://github.com/laguiar/rupt-bank-demo/workflows/Gradle%20tests/badge.svg)


## Rupt Bank Demo

**Rupt Bank Demo** is a small project covering just few scenarios of a bank account.

It's a Spring WebFlux + JPA + Kotlin project.
<p></br></p>
      
Functionalities exposed under ```/api/v1/accounts```:
 
```GET("")```: List all accounts, it's possible to filter the results using ```?type=VALUE1,VALUE2```.

```GET("/{iban}"```: Account details.

```GET("/{iban}/transactions)"```: List all account's transactions.

```POST("/{iban}/deposit")```: Perform a deposit on the given account.

```POST("/{iban}/transfer")```: Perform a transfer from the given account to another account.

```PUT("/{iban}/locker")```: Perform a lock/unlock operation on the given Account, depending on the current lock state.

<p></br></p>

---

#### Docker Image
To create and push docker image a register:
```
./gradlew jib --image=registry.hub.docker.com/<your-docker-user>/rupt-bank-demo
```
You can use Google GCR or Amazon ECR as you wish, just change the image path and have the credentials properly setting up on your environment.

To generate an image to a Docker daemon, to run/test locally:

```./gradlew jibDockerBuild```

<p></br></p>

#### Running it locally:

To quickly try it out, run the project with an embedded H2 database:

```./gradlew bootRun --args='--spring.profiles.active=h2'``` 

To do some development, the recommendation is to have a Postgres running locally.

If so, just run the application without any parameter:

```./gradlew bootRun```


Alternatively, Postgres can be setup to run on docker:
```
mkdir -p $HOME/docker/volumes/postgres
docker run --rm --name pg-docker -e POSTGRES_PASSWORD=docker -d -p 5432:5432 -v $HOME/docker/volumes/postgres:/var/lib/postgresql/data postgres
docker exec -i postgres psql -U postgres -c "CREATE DATABASE rupt-bank WITH ENCODING='UTF8' OWNER=postgres;"
```

Another alternative is to run the application with an H2 database instead of using Postgres.

<p></br></p>

#### Running the tests:

```./gradlew clean test```
