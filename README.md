# APIcast configopt
configopt for APIcast helps in the process of troubleshooting and optimising the [APIcast](https://github.com/3scale/apicast) configuration.

## Install
`git clone https://github.com/samugi/apicast-configopt.git`  
`cd apicast-configopt`  
`./mvnw clean compile assembly:single`  

## Start
`java -cp target/configopt-1.0-jar-with-dependencies.jar com.configopt.ConfigOpt --help`
