## Overview

This repo contains templates and a generator that will create boilerplate microservice starter projects based on other repos with a `ms-ref-*` prefix. The templates contain best practices described in [my blog](http://stevetarver.github.io/) and a stubbed in ReST endpoint for validation.

These are opinionated implementations targeting a Kubernetes cluster and attempt to balance including as much boilerplate and example code as possible without creating too much work yanking things out that one doesn't need in general.

## New Project Setup

Assuming you have a Kubernetes cluster and Jenkins build/deploy system, deploying a new service includes these steps:

* [ ] Create new GitHub repo & clone it
* [ ] Create a new service with this service generator
* [ ] Build and test the service locally
* [ ] Create an internal DNS entry as on ops endpoint for the new service in each cluster
* [ ] Create a Multibranch Jenkins build job for the service
* [ ] Commit the GitHub repo to initiate a build & deploy
* [ ] Verify the build, deploy, deployed functionality, ops endpoint access

Then: Implement business logic, iterate.

## Generating a new project skeleton

### Prerequisites

* Mac or *nix with bash / zsh shell 
* python 3.5+, curl, sed installed
* Shell access to GitHub

### Steps 

1. Create a new GitHub repo
1. Clone the repo
2. Open a bash / zsh shell and cd to GitHub repo dir
1. Run script: 
    ```
    curl https://raw.githubusercontent.com/stevetarver/ms-ref-templates/master/service_generator.py | python3
    ```
1. Provide requested info

## Template parameter concepts

Operational concerns are the most important consideration in service development. We want to support service implementations in all languages as a foundation for developer joy, but we need some boundaries so this doesn't impact operational chores. This motivates pervasive consistent naming.

We have the name uses:

* DNS names (internal ops endpoints)
* ReST URI paths
* java/groovy package names

That will also show up in:

* Deployment artifacts
* Kubernetes dashboards and inspection command responses
* Log records, queries, and visualizations
* Monitoring metrics, queries, and visualizations
* Distributed tracing metrics and visualizations
* Service mesh metrics, policies, etc.
* Ticketing systems
* Slack notifications
* PagerDuty
* etc.

To support uniform use, we'll introduce the following conventions

* **Prod DNS domain**: The production DNS Top Level Domain plus the domain name. E.g. 'makara.com'
* **Prod DNS host**: The production DNS Host portion of the DNS name; everything that precedes the dns_domain needed to reach the API ingress endpoint. E.g. 'api'. The DNS host + domain yield 'api.makara.com'.
* **Ops DNS domain**: An internal TLD and domain name that are the suffix for the ops endpoint. E.g. 'makara.dom'. The GitHub repo name is prefixed to form an ops endpoint providing greater access than the production endpoint; for example, access to health and metrics.
* **Business domain**: The business functional or logical domain. E.g. 'finance', 'hr', 'network'.
* **Business sub-domain**: A further clarification of the business domain to provide granularity to reduce growing service counts in any one business domain. E.g. The 'finance' domain could have 'payroll' and 'reporting' sub-domains.

Using these four pieces of information, we can construct:

* **Prod DNS name**: {prod_dns_host}.{prod_dns_domain}: 'api.makara.com'
* **Ops DNS name**: {github_repo_name}.{ops_dns_domain}: 'fin-payroll.makara.dom'
* **ReST URI paths**: {business_domain}/{business_subdomain}/{resource}: '/finance/payroll/employees'
* **Root package name**: {dns_domain_reversed}.{business_domain}.{business_subdomain}: 'com.makara.finance.payroll'

## Example run


## TODO

* Replace `./README.md` with service specific content
* Provide generic integration testing stubs
    * keyword replacement in integration test


