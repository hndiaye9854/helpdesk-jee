# HelpDesk JEE

Application web de gestion de tickets informatiques.

## Stack
- Java 21, Jakarta EE 10 (Servlet 6, JSP 3.1, JSTL 3)
- JPA 3.1 / Hibernate 6, PostgreSQL 16, Flyway
- Maven, Tomcat 10.1, Docker, GitHub Actions

## Prérequis
- JDK 21
- Maven 3.9+
- Docker Desktop

## Lancer en local
1. Copier `.env.example` en `.env`
2. Démarrer la base : `docker compose -f compose.dev.yaml up -d`
3. Construire : `mvn clean package`

## Workflow Git
- `main` : production
- `develop` : intégration
- `feature/*` : développements, fusionnés dans `develop` par Pull Request