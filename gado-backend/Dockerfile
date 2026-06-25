# Build da aplicação usando Maven e Java 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia o pom.xml principal e os códigos de todos os módulos
COPY pom.xml .
COPY gado-domain ./gado-domain
COPY gado-application ./gado-application
COPY gado-infrastructure ./gado-infrastructure
COPY gado-api ./gado-api

# Executa o build do Maven gerando o arquivo .jar (pulando os testes que o GitHub já rodou)
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o arquivo gerado no estágio anterior para a imagem final
COPY --from=build /app/gado-api/target/*.jar app.jar

# Expõe a porta padrão do Spring Boot (se a sua for outra, mude aqui)
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]