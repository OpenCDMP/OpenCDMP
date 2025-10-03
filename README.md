# Introduction

**OpenCDMP** is an open and extensible software platform designed to simplify the management, monitoring, and maintenance of **Output Management Plans (OMPs)**, such as **Data Management Plans (DMPs)** and **Software Management Plans (SMPs)**. It provides a flexible and customizable environment that streamlines the complex processes involved in handling OMPs, ensuring efficiency, compliance, and collaboration across various projects and organizations.

## 📖 **Documentation**

**[View the complete documentation →](https://opencdmp.github.io/)**

For detailed guides, configuration references, and API documentation, visit our comprehensive documentation site.

## 🚀 **Key Features**

- **Modular Architecture**: Easily extend the platform with new modules and plugins to meet specific needs.
- **Evaluation via Plugins**: Perform evaluation through customizable plugins tailored to specific standards and requirements.
- **Collaborative Environment**: Supports teamwork with multi-user access, version control, and change tracking.
- **Role-Based Access Control**: Invite users to a Plan with different roles—Viewer, Contributor, Reviewer, etc.—and set access rights at the Plan or Section level.
- **Review and Annotation System**: Reviewers can add annotations (comments) to Plans or Descriptions, with statuses to track their lifecycle.
- **Notification Features**: Receive Email and In-App notifications for updates, comments, and changes to Plans or Descriptions you are involved in.
- **Flexible Export Options**: Export Plans in human and machine-readable formats like XML, JSON, DOCX, and PDF.
- **Pluggable Export Mechanism**: Implement custom export plugins to suit specific requirements.
- **Repository Deposits for DOI Assignment**: Deposit OMPs directly to repositories for DOI (Digital Object Identifier) assignment.
- **Pluggable Deposit Mechanism**: Implement custom deposit plugins to integrate with different repositories.
- **Customizable Templates**: Offers a library of templates for various types of OMPs, customizable to fit specific project requirements.
- **Integration Capabilities**: Seamlessly connects with other software tools and platforms, enabling data import/export and interoperability via APIs.

## 🧩 **Core Concepts**

  - #### **Blueprints**

    Define the structure of a Plan by specifying its Sections and content.

  - #### **Plans**

    Primary entities representing comprehensive OMPs that guide research outputs throughout their lifecycle.

  - #### **Description Templates**

    Define the structure of Descriptions within a Plan, specifying required fields and content.

  - #### **Descriptions**

    Detailed entries providing specific information about research inputs, outputs, and processes within a Plan.

## 🏗️ **Architecture**

OpenCDMP uses a **microservices architecture** with the following main components:

- **API Service**: Main backend (Java/Spring Boot)
- **Webapp Service**: Frontend application (Angular)
- **Notification Service**: Email and in-app notifications
- **Annotation Service**: Comments and review system
- **Repository Deposit Services**: Pluggable DOI assignment (Zenodo, Dataverse, etc.)
- **File Transformation Services**: Pluggable export/import (XML, JSON, DOCX, PDF)
- **Keycloak**: OAuth2/OIDC authentication server
- **PostgreSQL**: Primary database
- **RabbitMQ**: Message broker for inter-service communication
- **Elasticsearch**: Search and indexing

For detailed architecture diagrams and workflows, see the [Architecture Overview](https://opencdmp.github.io/getting-started/architecture/).

## 🚀 **Getting Started**

### Quick Deployment

The easiest way to deploy OpenCDMP is using our **docker-deployment** repository with pre-configured Docker Compose files:

```bash
git clone https://github.com/OpenCDMP/docker-deployment.git
cd docker-deployment
docker-compose up -d
```

**[View Deployment Guide →](https://opencdmp.github.io/getting-started/deployment/)**

### Requirements

- **Node.js**: >= 18.0 (for frontend development)
- **Java**: >= 21 (for backend development)
- **Docker**: For containerized deployment
- **PostgreSQL**: >= 12
- **Keycloak**: >= 20.0

### Documentation Resources

- **[Getting Started Guide](https://opencdmp.github.io/getting-started/)** - Platform introduction and deployment
- **[User Guide](https://opencdmp.github.io/user-guide/)** - End-user documentation
- **[Admin Guide](https://opencdmp.github.io/admin-guide/)** - Administrator setup and configuration
- **[Developer Guide](https://opencdmp.github.io/developers/)** - For those interested in extending the platform
- **[API Documentation (Swagger)](https://opencdmp.github.io/developers/swagger/)** - Interactive API reference
- **[Configuration Reference](https://opencdmp.github.io/getting-started/configuration/)** - Complete configuration options
- **[Troubleshooting](https://opencdmp.github.io/troubleshooting/)** - Common issues and solutions

## ✨ **Benefits**

- **Efficiency**: Simplifies the creation and management of Output Management Plans, saving time and reducing complexity.
- **Enhanced Collaboration**: Facilitates communication and cooperation among team members with shared access and collaborative features.
- **Quality Control**: Ensures high-quality OMPs through structured templates, review workflows, and validation mechanisms.
- **Flexible Sharing and Distribution**: Easily export and share Plans in multiple formats suitable for different audiences.
- **Customizability**: Adapts to the unique needs of different projects, disciplines, and organizations.
- **Stay Informed**: Keep track of all changes and updates with comprehensive notification features.
- **Flexibility and Scalability**: Scales to accommodate projects of all sizes, from small teams to large organizations.

## 🤝 **Community and Support**

- **GitHub Repository**: [https://github.com/OpenCDMP/OpenCDMP](https://github.com/OpenCDMP/OpenCDMP)
- **Documentation**: [https://opencdmp.github.io/](https://opencdmp.github.io/)
- **Issue Tracker**: Report bugs and request features via [GitHub Issues](https://github.com/OpenCDMP/OpenCDMP/issues)
- **Email**: opencdmp at cite.gr

## 📄 **License**

This project is licensed under the **EUPL 1.2 License**. See the [LICENSE](LICENSE) file for details.

## 🌐 **Contact**

For questions or support, please contact:
- **Email**: opencdmp at cite.gr
---

**Start simplifying your Output Management Plans with OpenCDMP today!**
