#!/usr/bin/env bash

set -e

cd "$(dirname "$0")"
BASEDIR="$(pwd)"

function update_release() {
    prj="$1"
    tag="$2"
    cd "${BASEDIR}/subprojects/${prj}"
    git checkout "${tag}"
}

update_release cdi 4.0.1
update_release common-annotations-api 2.1.1
update_release expression-language 5.0.0-RELEASE-api
update_release injection-api 2.0.1
update_release interceptors 2.1.0
update_release jaf-api 2.0.1
update_release jaxb-api 3.0.1
update_release rest 3.1.0
update_release servlet 6.0.0-RELEASE
update_release validation 3.0.2
