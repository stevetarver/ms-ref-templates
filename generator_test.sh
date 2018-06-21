#!/usr/bin/env bash
#
# Simple script for service_generator.py local testing
#

# execute service_generator from anywhere without losing cwd
MY_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
${MY_DIR}/service_generator.py      \
    --template=java-spring-maven    \
    --prod_dns_domain=makara.com    \
    --prod_dns_host=api             \
    --ops_dns_domain=makara.dom     \
    --bus_domain=finance            \
    --bus_subdomain=payroll
