# LC Capture Tool

Video Demo: https://youtu.be/UyPV7N6FHfE

## Local Development Environment (Not IDE Specific)
1. Install Java 16, Maven, Docker, and Docker Compose
2. Setup datasource. See `src/main/resources/docker/docker-compose.yml` for a sample MariaDB docker container. Update the volumes path, replacing `/Users/joey/lyrasis/mysql` with a path on your machine. Start the container with `docker-compose up`
3. Update settings in `/src/main/resources/application-dev.properties` to match your local development environment. Namely, update the `lct.path.*` settings 
4. Run the application using the Spring Boot Maven plugin. To do this, navigate to the main directory containing the `pom.xml` and run `mvn spring-boot:run -Dspring-boot.run.profiles=dev`. This will start the application.

## Local Development Environment (IntelliJ)
1. Install Java 16, Maven, Docker, and Docker Compose
2. In IntelliJ, open the `pom.xml` and import as a Maven project
3. Open `src/main/resources/docker/docker-compose.yml` for a sample MariaDB docker container. Update the volumes path, replacing `/Users/joey/lyrasis/mysql` with a path on your machine. Start the container with the IDE's 'Run' button
4. Edit the Run Configuration using the 'Edit Configuration' option in the dropdown in the top right of the IDE, near the Start & Debug buttons
5. Add `dev` to the Active Profiles section. Override parameters from `src/main/resources/application-dev.properties` in the "Override paramaters" section as needed to match your local setup
6. Run the application with either the Start or Debug button

## Developer Profile
A Developer profile `dev`  (mentioned above) is included to make local development easier. This profile does the following differently from the `remote` profile used in production:
- Thymleaf HTML templates and static resources are updated on page reload, so you can make changes to them without restarting the application
- The `DevelopmentUserService` bean is enabled and the regular `UserService` and `FirebaseAuth` beans are disabled. This removes the need to include Firebase Admin secrets by default. You will always be logged in as the user set in `DevelopmentUserService`

You will also likely need to update some parameters in `src/main/resources/application-dev.properties`
- lct.path.* settings should be updated or overridden to reflect your local machine
- jdbc settings may need to be changed to reflect your datasource setup
