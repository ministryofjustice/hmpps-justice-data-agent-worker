# hmpps-justice-data-agent-worker

[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/hmpps-justice-data-agent-worker/badge?style=flat)](https://github-community.service.justice.gov.uk/repository-standards/hmpps-justice-data-agent-worker)
[![Docker Repository on ghcr](https://img.shields.io/badge/ghcr.io-repository-2496ED.svg?logo=docker)](https://ghcr.io/ministryofjustice/hmpps-justice-data-agent-worker)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://justice-data-agent-worker-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html)

Service to add generic service to integrate with LLM.

# Instructions

Run docker compose to add postgresql db and localstack to run service locally.
The service run on port 8080.
For API authentication either run hmpps-auth locally or connects to hmpps-auth dev environment using environment values.

### Slack channel for release notifications

By default, release notifications are only enabled for production. The circleci configuration can be amended to send
release notifications for deployments to other environments if required. Note that if the configuration is amended,
the slack channel should then be amended to your own team's channel as `dps-releases` is strictly for production release
notifications. If the slack channel is set to something other than `dps-releases`, production release notifications
will still automatically go to `dps-releases` as well. This is configured by `releases-slack-channel` in
`.circleci/config.yml`.

### Slack channel for pipeline security notifications

Ths channel should be specific to your team and is for daily / weekly security scanning job results. It is your team's
responsibility to keep up-to-date with security issues and update your application so that these jobs pass. You will
only be notified if the jobs fail. The scan results can always be found in circleci for your project. This is
configured by `alerts-slack-channel` in `.circleci/config.yml`.

### Non production kubernetes alerts

By default Prometheus alerts are created in the application namespaces to monitor your application e.g. if your
application is crash looping, there are a significant number of errors from the ingress. Since Prometheus runs in
cloud platform AlertManager needs to be setup first with your channel. Please see
[Create your own custom alerts](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/monitoring-an-app/how-to-create-alarms.html)
in the Cloud Platform user guide. Once that is setup then the `custom severity label` can be used for
`alertSeverity` in the `helm_deploy/values-*.yaml` configuration.

Normally it is worth setting up two separate labels and therefore two separate slack channels - one for your production
alerts and one for your non-production alerts. Using the same channel can mean that production alerts are sometimes
lost within non-production issues.

### Production kubernetes alerts

This is the severity label for production, determined by the `custom severity label`. See the above
[Non production kubernetes alerts section](non-production-kubernetes-alerts) for more information. This is configured in `helm_deploy/values-prod.yaml`.

### Product ID

This is so that we can link a component to a product and thus provide team and product information in the Developer
Portal. Refer to the developer portal at <https://developer-portal.hmpps.service.justice.gov.uk/products> to find your
product id. This is configured in `helm_deploy/<project_name>/values.yaml`.

## Manually branding from template app

Run the `rename-project.bash` without any arguments. This will prompt for the six required parameters and create a PR.
The script requires a recent version of `bash` to be installed, as well as GNU `sed` in the path.

## Common Kotlin patterns

Many patterns have evolved for HMPPS Kotlin applications. Using these patterns provides consistency across our suite of
Kotlin microservices and allows you to concentrate on building your business needs rather than reinventing the
technical approach.

Documentation for these patterns can be found in the [HMPPS tech docs](https://tech-docs.hmpps.service.justice.gov.uk/common-kotlin-patterns/).
If this documentation is incorrect or needs improving please report to [#ask-prisons-digital-sre](https://moj.enterprise.slack.com/archives/C06MWP0UKDE)
or [raise a PR](https://github.com/ministryofjustice/hmpps-tech-docs).

## Running the application locally

The application comes with a `dev` spring profile that includes default settings for running locally. This is not
necessary when deploying to kubernetes as these values are included in the helm configuration templates -
e.g. `values-dev.yaml`.

There is also a `docker-compose.yml` that can be used to run a local instance of the template in docker and also an
instance of HMPPS Auth (required if your service calls out to other services using a token).

```bash
docker compose pull && docker compose up
```

will run the application and HMPPS Auth within a local docker instance.

### Running the application in Intellij

```bash
docker compose pull && docker compose up --scale hmpps-justice-data-agent-worker=0
```

will just start a docker instance of HMPPS Auth. The application should then be started with a `dev` active profile
in Intellij.

### Building and running the docker image locally

The `Dockerfile` relies on the application being built first. Steps to build the docker image:
1. Build the jar files
```
./gradlew clean assemble
```
2. Copy the jar files to the base directory so that the docker build can find them
```
cp build/libs/*.jar .
```
3. Build the docker image with required arguments
```
docker build --build-arg GIT_REF=21345 --build-arg GIT_BRANCH=bob --build-arg BUILD_NUMBER=$(date '+%Y-%m-%d') .
```
4. Run the docker image, setting the auth url so that it starts up
```
docker run -e HMPPS_AUTH_URL="https://sign-in-dev.hmpps.service.justice.gov.uk/auth" <sha from step 3>
```
