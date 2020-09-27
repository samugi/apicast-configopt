# APIcast configopt
Configopt for APIcast helps in the process of troubleshooting and optimising the configuration for [APIcast](https://github.com/3scale/apicast).

## Install
```
git clone https://github.com/samugi/apicast-configopt.git
cd apicast-configopt
./mvnw clean compile assembly:single
```

## Start
`java -cp target/configopt-1.0-jar-with-dependencies.jar com.configopt.ConfigOpt --help`
