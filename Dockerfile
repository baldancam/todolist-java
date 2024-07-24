# Use uma imagem base do OpenJDK
FROM openjdk:17-jdk-slim

# Crie um diretório de trabalho
WORKDIR /app

# Copie o arquivo JAR da aplicação para o contêiner
COPY target/todolist-1.0.0.jar app.jar

# Exponha a porta que a aplicação usará
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java","-jar","/app/app.jar"]
