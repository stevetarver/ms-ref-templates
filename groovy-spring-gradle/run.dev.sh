#!/usr/bin/env bash
#
# Run this project with human readable, colored logging
#
# -Dspring.profiles.active=dev - sets the active profile to dev to enable colored
#                                logging.
#
# -Dspring.output.ansi.enabled=always - stops gradle from eating spring colors.
#
# --console=plain - a simple scrolling output for console logging. Eliminates
#                   the window funkiness with the gradle stuff at the bottom,
#                   allows you to put some whitespace in the output to clearly
#                   see log entries made after whitespace, etc.
#
# bootRun - run the Springboot application
#

./gradlew --console=plain -Dspring.profiles.active=dev -Dspring.output.ansi.enabled=always bootRun