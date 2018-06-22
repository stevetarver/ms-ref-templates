#!/usr/bin/env bash
#
# Run this project with human readable, colored logging
#
# -Dspring.profiles.active=dev - sets the active profile to dev to enable colored
#                                logging.
#
# bootRun - run the Springboot application
#

mvn -Dspring.profiles.active=dev spring-boot:run
