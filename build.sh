#!/bin/sh

# build.sh
#
# Created by Quan MT 2014 www.quaninte.com
PROJDIR='./'

# license key
LICENSE=$1
VERSION=$6

# Sign params
KEYSTORE=$2
KEYSTORE_PASSWORD=$3
KEY_ALIAS=$4
KEY_PASSWORD=$5

# Export ENV params
export GRADLE_KEYSTORE="${KEYSTORE}"
export GRADLE_KEYSTORE_PASSWORD="${KEYSTORE_PASSWORD}"
export GRADLE_KEY_ALIAS="${KEY_ALIAS}"
export GRADLE_KEY_PASSWORD="${KEY_PASSWORD}"

# target file
TARGET_FILE="${PWD}/files/${LICENSE}/SocialApp-${VERSION}.apk"

# compile project
echo "Building Project"
cd "${PROJDIR}"
echo "running ./gradlew assembleRelease"
./gradlew assembleRelease

# Copy apk files to build folder
mkdir -p "files/${LICENSE}"
cp "socialapp/build/outputs/apk/socialapp-release.apk" "${TARGET_FILE}"

echo "APK file is exported at"
echo ${TARGET_FILE}