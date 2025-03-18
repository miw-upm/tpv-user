## [Máster en Ingeniería Web por la Universidad Politécnica de Madrid (miw-upm)](http://miw.etsisi.upm.es)

## Back-end con Tecnologías de Código Abierto (BETCA).

> Este proyecto es un apoyo docente de la asignatura y contiene un proyecto completo con Spring.

Es un ejemplo de un API Rest completo, basado en Spring Boot, con una arquitectura de tres capas, y almacenamiento en bases de datos con JPA soportado por Hibernate y Postgres.
La seguridad esta basada en OAuth2 y OpenId Connect, desarrollando ambos procesos en el API

## Tecnologías necesarias

`Java` `Maven` `GitHub` `Spring-Boot` `Sonarcloud` `JPA` `PostgreSQL` `Docker` `OAuth2` `OpenID Connect`

### :gear: Instalación del proyecto

1. Clonar el repositorio en tu equipo, **mediante consola**:

```sh
> cd <folder path>
> git clone https://github.com/miw-upm/tpv-user
```

2. Importar el proyecto mediante **IntelliJ IDEA**
    * **Open**, y seleccionar la carpeta del proyecto.

### :gear: Ejecución en local

* Ejecutar en consola del proyecto el siguiente comando de Docker:

```sh
> docker compose up --build -d
```
* Necesita de una bases de datos: **url:** `jdbc:postgresql://localhost:5432/tpvuser`, **username:**`postgres` y **password:** `postgres`. Recordar que la BD **tpvuser** se deben crear manualmente.

* Se aporta un fichero `docker-compose-db.yml`que monta 3 motores de BD sobre Docker: Postgres, MySQL y MongoDB.
```sh
> docker compose -f docker-compose-db.yml -p databases up -d
```

