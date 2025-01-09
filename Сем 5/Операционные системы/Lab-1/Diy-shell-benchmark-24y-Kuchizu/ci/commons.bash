#!/bin/bash

BLUE='\033[0;34m'
RED='\033[31m'
NC='\033[0m'

function message() {
    echo -e "${BLUE}[Lab-1] $1${NC}"
}

function error() {
    echo -e "${RED}[Lab-1] Error: $1${NC}"
}
