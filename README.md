# APIcast configopt
Configopt for APIcast helps in the process of troubleshooting and optimising the configuration for [APIcast](https://github.com/3scale/apicast).

## Install
```
git clone https://github.com/samugi/apicast-configopt.git
cd apicast-configopt
go build
```

## Start
`./configopt --help`


## Features
- Overview of issues found in the configuration ordered by severity (at the moment limited to conflicts in the mapping rules)
- Scan for issues based on host routing / path routing / path routing only modes
- Interactive mode to scan the configuration and output the fixed version

## Known issues:
- Outputed configurations are missing most fields from the outer objects and are only reliable for the mapping rules structure
