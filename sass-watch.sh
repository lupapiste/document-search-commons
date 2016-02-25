#!/bin/sh

sass -r compass-core -t nested --compass --sourcemap=file --watch scss:resources/public/css
