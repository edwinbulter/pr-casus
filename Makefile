# Entry point for the daily loop and for CI. The version is the short commit hash, with -dirty for uncommitted work.
VERSION  ?= $(shell git rev-parse --short HEAD)$(shell test -z "$$(git status --porcelain)" || echo -dirty)
PERF_SIZES ?= 10000,20000,100000
PLATFORM ?= $(if $(filter arm64 aarch64,$(shell uname -m)),linux/arm64,linux/amd64)
MVN      := ./mvnw -B -ntp

.PHONY: help commit-stage image acceptance-tests acceptance run version perf

help: ## Overview of the targets
	@grep -E '^[a-z-]+:.*## ' $(MAKEFILE_LIST) | awk -F ':.*## ' '{printf "  %-17s %s\n", $$1, $$2}'

commit-stage: ## Unit and integration tests; the acceptance module only compiles
	$(MVN) verify

image: ## Build the service image three-kids:<version> in the local Docker daemon (Jib)
	$(MVN) -pl service package jib:dockerBuild -DskipTests -Dapp.version=$(VERSION) -Djib.from.platforms=$(PLATFORM)

acceptance-tests: ## Run the acceptance scenarios against three-kids:<version> (build it first)
	$(MVN) -pl acceptance verify -DskipITs=false -Dapp.version=$(VERSION)

acceptance: image acceptance-tests ## Build the image of this version and run the acceptance scenarios against it

run: image ## Run the service on http://localhost:8080
	docker run --rm -p 8080:8080 three-kids:$(VERSION)

perf: ## Measure the time per registration in-process (not part of the pipeline); sizes with PERF_SIZES=10000,100000
	$(MVN) -q -pl service test -Dsurefire.excludedGroups= -Dgroups=perf -Dtest=RegistrationMeasurement -Dperf.sizes=$(PERF_SIZES)

version: ## The version of this build
	@echo $(VERSION)
