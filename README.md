# Archived: apicast-configopt is now part of [3scale-insights-rules](https://gitlab.cee.redhat.com/red-hat-3scale-support/3scale-insights-rules)

# APIcast configopt
Configopt for APIcast helps in the process of troubleshooting and optimising the configuration for [APIcast](https://github.com/3scale/apicast).

## Install
```
git clone https://github.com/samugi/apicast-configopt.git
cd apicast-configopt
go build -o configopt
```

## Start
`./configopt --help`

## Docker (no Go required)
### Running on Docker - examples 
**Simple run**
```
$ docker run --rm golang sh -c "go get github.com/samugi/apicast-configopt/... && exec apicast-configopt --help"
```

**Config scan example**
```
$ docker run -v $(pwd)/YOUR_CONFIGURATION.json:/go/config.json --rm golang sh -c "go get github.com/samugi/apicast-configopt/... && exec apicast-configopt -c /go/config.json"
```

**Output example**
```
$ docker run -v $(pwd)/YOUR_CONFIGURATION.json:/go/config.json -v $(pwd):/go/output:rw --rm golang sh -c "go get github.com/samugi/apicast-configopt/... && exec apicast-configopt -c /go/config.json -o /go/output/result.txt"
```

**Interactive mode with output example**
```
$ docker run -i -v $(pwd):/go/output:rw -v $(pwd)/YOUR_CONFIGURATION.json:/go/config.json --rm golang sh -c "go get github.com/samugi/apicast-configopt/... && exec apicast-configopt -c /go/config.json -i -o /go/output/output.json"
```

### Build with Docker, execute locally (Linux only)
```
$ docker run -v $(pwd):/go/bin golang go get github.com/samugi/apicast-configopt/…
$ ./apicast-configopt --help
```

## Features
- Overview of issues found in the configuration ordered by severity (at the moment limited to mapping rules conflicts)
- Scan for issues based on host routing / path routing / path routing only modes
- Interactive mode: to scan the configuration and output the fixed version
- Update-remote: to update the configuration on your 3scale instance
