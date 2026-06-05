# syntax=docker/dockerfile:1

# ─────────────────────────────────────────────────────────────────────────────
# Estágio de build — compila o projeto Maven multi-módulo e empacota o backend.
# Contexto de build esperado: ./Projeto (raiz do projeto Maven, com o pom-pai).
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copia todo o reator (pom-pai + módulos). O .dockerignore mantém o contexto enxuto.
COPY . .

# Empacota só o backend e os módulos dos quais ele depende (-am), sem tests.
# O goal `repackage` do spring-boot-maven-plugin gera o JAR executável.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl apresentacao-backend -am -Dmaven.test.skip=true package

# ─────────────────────────────────────────────────────────────────────────────
# Estágio de runtime — apenas o JRE + o JAR empacotado.
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

# Copia o JAR executável do backend (o *.jar.original não casa com *.jar).
COPY --from=build /build/apresentacao-backend/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
