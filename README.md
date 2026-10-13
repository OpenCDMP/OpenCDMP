# OpenCDMP

OpenCDMP is an open-source platform for writing, reviewing, and publishing Output Management Plans (OMPs), such as Data Management Plans (DMPs) and Software Management Plans (SMPs). Administrators define what a plan asks with plan blueprints and asset templates; researchers fill in plans together, evaluate them, download them, and publish them to repositories to get a DOI.

This repository holds the core of the platform: the API service (Java, Spring Boot), the web application (Angular), and the database migrations. The plugins (file transformers, deposit services, evaluators) and the shared libraries live in their own repositories under the [OpenCDMP organisation](https://github.com/OpenCDMP).

**Documentation: [opencdmp.github.io](https://opencdmp.github.io/)**

## Features

- Invite collaborators to a whole plan or to some of its sections, with roles such as Editor, Reviewer, or Viewer, and discuss the content in comments.
- Define what a plan and each of its assets ask, with plan blueprints and asset templates.
- Move plans through the statuses and transitions each organisation defines, keep versions of a plan, and compare two versions field by field.
- Download plans as PDF, DOCX, JSON, or XML, and publish them to repositories such as Zenodo to get a DOI.
- Check plans against standards such as the RDA maDMP schema with evaluator plugins.
- Add file-transformer, deposit, and evaluator plugins, and connect other systems through the REST API and webhooks.
- Run many organisations on one installation, each with its own users, templates, workflows, branding, and settings.
- Notify users by email and in the app.

## Core concepts

- **Plan blueprints** define the structure of a plan: its sections and fields.
- **Plans** are the OMPs themselves, from the first draft to a finalized, published version.
- **Asset templates** define the questions of an asset.
- **Assets** describe one research output, input, or process of a plan, such as a dataset or a piece of software.

## Architecture

OpenCDMP runs as a set of services:

- **Webapp**: the Angular frontend (`frontend/`).
- **API service**: the Spring Boot backend (`backend/`).
- **Notification** and **annotation** services: email and in-app notifications, and comments. They are distributed as Docker images only.
- **Plugins**: file transformers (export and import), deposit services (publishing to repositories), and evaluators.
- **Infrastructure**: PostgreSQL, Elasticsearch, RabbitMQ, Keycloak for authentication, and Gotenberg for PDF conversion.

See the [architecture overview](https://opencdmp.github.io/deployment/architecture/) for how they connect.

## Try it with Docker

The [docker-deployment](https://github.com/OpenCDMP/docker-deployment) repository starts a complete installation on one machine with Docker Compose:

```bash
git clone https://github.com/OpenCDMP/docker-deployment.git
cd docker-deployment
docker compose up -d
```

The example is for local evaluation only. See [Installation](https://opencdmp.github.io/deployment/install/) for the full walkthrough and [Before going to production](https://opencdmp.github.io/deployment/install/production/) for what a real installation needs.

Released images are on Docker Hub as [`opencdmp/opencdmp-api`](https://hub.docker.com/r/opencdmp/opencdmp-api) and [`opencdmp/opencdmp-webapp`](https://hub.docker.com/r/opencdmp/opencdmp-webapp).

## Build from source

| Tool | Version |
| --- | --- |
| Java | 21 |
| Maven | 3.9 |
| Node.js | 24.15 or later, or 22.22 or later on the 22 line |
| Docker | to build the images and run the dependencies |

A running instance also needs PostgreSQL, Elasticsearch with the `analysis-icu` plugin, RabbitMQ, and Keycloak. The Docker example uses PostgreSQL 17, Elasticsearch 9.1, RabbitMQ 4.1, and Keycloak 26.1.

### Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run -pl web -Dspring-boot.run.profiles=devel
```

The API listens on `http://localhost:8081/api`, with the Swagger UI at `http://localhost:8081/api/swagger-ui`. It creates and migrates its database schema at startup. All its dependencies are on Maven Central.

To point the API at your own PostgreSQL, Elasticsearch, RabbitMQ, and Keycloak, create `app-devel.env` or `<name>-devel.yml` files (for example `db-devel.yml`) in `web/src/main/resources/config/` or in a `config/` folder next to the working directory. The `devel` profile reads them when they exist. `app.env` and the `.yml` files next to it list every setting, and [Backend configuration](https://opencdmp.github.io/deployment/configuration/backend/) describes them.

### Frontend

```bash
cd frontend
npm install
npm start           # development server on http://localhost:4200
npm run build       # production build in dist/
```

The webapp reads `src/assets/config/config.json`, overlaid by `src/assets/config/config-override.json` when that file exists. For local development, set the API, Keycloak, notification, and annotation addresses in the override file. See [Frontend configuration](https://opencdmp.github.io/deployment/configuration/frontend/).

### Docker images

`backend/Dockerfile` and `frontend/Dockerfile` build the API and the webapp from source, without the steps above:

```bash
docker build -t opencdmp-api backend
docker build -t opencdmp-webapp frontend
```

## Repository layout

| Path | Contents |
| --- | --- |
| [`backend/`](backend/) | The API service: Maven modules `core` (entities, queries, services) and `web` (controllers, configuration, Flyway migrations) |
| [`frontend/`](frontend/) | The Angular webapp |
| [`THEMING.md`](THEMING.md) | The CSS variables an organisation can override in its branding |

## Releases and upgrades

Releases are tagged `X.Y.Z`. The [changelog](https://opencdmp.github.io/deployment/install/changelog/) lists what changed in each release and which service versions belong together. The [upgrade guide](https://opencdmp.github.io/deployment/install/upgrade/) covers moving an installation to a new version, including the database migrations.

## Update check and usage data

Once a day, an installation sends its version and a random installation ID to the OpenCDMP update service, and shows the latest release and project notices on its [Maintenance](https://opencdmp.github.io/administering/maintenance/#installation) page. By default the check also carries anonymous usage data: the installation's address, the number of organisations, users, plans and assets, and the Java and database versions. A system administrator can stop the usage data on the Maintenance page; `UPDATE_CHECK_ENABLED=false` switches the check off entirely.

## Documentation

- [Using OpenCDMP](https://opencdmp.github.io/using/): creating and managing plans and assets
- [Administering OpenCDMP](https://opencdmp.github.io/administering/): blueprints, templates, users, organisations, and workflows
- [Deployment and configuration](https://opencdmp.github.io/deployment/): installation, configuration of every service, upgrades
- [API and integration](https://opencdmp.github.io/developers/): [API usage](https://opencdmp.github.io/developers/api-usage/), [Swagger](https://opencdmp.github.io/developers/swagger/), and [plugin development](https://opencdmp.github.io/developers/plugins/)
- [Troubleshooting](https://opencdmp.github.io/troubleshooting/)

## Contributing and support

Report bugs and request features in [GitHub Issues](https://github.com/OpenCDMP/OpenCDMP/issues), and read [Contributing](https://opencdmp.github.io/developers/contributing/) before opening a pull request.

Questions: email opencdmp at cite.gr.

## License

OpenCDMP is licensed under the [European Union Public Licence (EUPL) v1.2](LICENSE).
