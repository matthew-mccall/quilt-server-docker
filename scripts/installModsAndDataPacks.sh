#!/bin/sh

bootstrapArgs="$@" # I declare a motion to create a new shell with more intuitive substitution rules
gradle --project-dir ./scripts/bootstrapper run --args="$bootstrapArgs"