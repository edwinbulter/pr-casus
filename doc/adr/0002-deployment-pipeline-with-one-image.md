# 0002. Deployment pipeline: build one image with Jib, accept exactly that image

Date: 2026-10-25
Status: Accepted

## Context
The walking skeleton needs a deployable artefact and a pipeline that tests it. The acceptance tests must run against what would be delivered, not against code in a test JVM, and must prove that they test the build that was just made. The target environment is a Docker container; Kubernetes would add a lot of work for a small service.

Options for the image:
1. **Jib** (`jib-maven-plugin`): no Dockerfile, layers for dependencies, resources and classes, reproducible; also used in banksim-mse.
2. **`spring-boot:build-image`** (Cloud Native Buildpacks): no extra plugin, but slower and the image is less explicit.
3. **A Dockerfile**: most explicit, but we write and maintain it ourselves.

## Decision
- The image is built once with Jib, on a distroless Java 21 base image pinned by digest, running as non-root.
- The version of a build is the short commit hash, with `-dirty` for uncommitted work. It is the image tag and is reported by `GET /api/version` (environment variable `APP_VERSION`).
- GitHub Actions has two stages. The **commit stage** runs `make commit-stage` and builds the image. The **acceptance stage** starts only when the commit stage is green, loads exactly that image and runs `make acceptance-tests`.
- Locally and in CI the same `Makefile` targets are used.

## Consequences
- What the acceptance stage tests is the artefact that would be delivered.
- The image is built for the platform of the machine (`linux/arm64` on Apple Silicon, `linux/amd64` in CI); a multi-platform image is not needed now.
- `make image` skips the tests. Run `make commit-stage` first.

## Guard
- The acceptance scenario `ServiceVersionScenarios` starts the image tagged with the current version and asserts that `GET /api/version` reports that version. An old or missing image makes it fail.
- The pipeline has been seen red once, with the acceptance stage not running after a failing commit stage.
