FROM sbtscala/scala-sbt:17.0.2_1.6.2_3.1.3

COPY ./src ./src
COPY ./build.sbt ./build.sbt
COPY ./project/build.properties /project
COPY ./project/plugins.sbt /project

RUN sbt compile

EXPOSE 8080

CMD ["sbt", "run"]