# Build da aplicação usando Maven e Java 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia o pom.xml e o código-fonte (módulo único)
COPY pom.xml .
COPY src ./src

# Executa o build do Maven gerando o arquivo .jar (pulando os testes que o GitHub já rodou)
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o arquivo gerado no estágio anterior para a imagem final
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta padrão do Spring Boot (se a sua for outra, mude aqui)
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
