#!/bin/bash
set -e
./package.sh
eb deploy
rm app.zip
