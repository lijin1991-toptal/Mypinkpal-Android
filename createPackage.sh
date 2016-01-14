#!/bin/bash

PACKAGE_NAME=$1
VERSION=$2
VERSION_CODE=$3

while true; do
    if [ -n "${PACKAGE_NAME}" ]; then
        break
    fi

	echo "Enter package name: ";
	read PACKAGE_NAME
done

SRC_PATH="${PWD}/socialapp/src"
PACKAGE_FOLDER="${SRC_PATH}/main/java/${PACKAGE_NAME//.//}"

# Create package
echo "Creating package path ${PACKAGE_FOLDER}"
mkdir -p "${PACKAGE_FOLDER}"

# Copy AppActivity file
cp "${SRC_PATH}/main/java/com/mypinkpal/app/AppActivity.java" "${PACKAGE_FOLDER}/AppActivity.java"
cp "${SRC_PATH}/main/java/com/mypinkpal/app/GCMIntentService.java" "${PACKAGE_FOLDER}/GCMIntentService.java"

# Replace content of AppActivity.xml
ACTIVITY_FILE="${PACKAGE_FOLDER}/AppActivity.java"
ACTIVITY_GCM="${PACKAGE_FOLDER}/GCMIntentService.java"

echo "Processing file ${ACTIVITY_FILE}"
sed -i '' "s/com.mypinkpal.app/${PACKAGE_NAME}/g" "${ACTIVITY_FILE}"
sed -i '' "s/com.mypinkpal.app/${PACKAGE_NAME}/g" "${ACTIVITY_GCM}"

# Replace content of AndroidManifest.xml
MANIFEST_FILE="${SRC_PATH}/main/AndroidManifest.xml"
echo "Processing file ${MANIFEST_FILE}"
sed -i '' "s/package=\".*\"/package=\"${PACKAGE_NAME}\"/g" "${MANIFEST_FILE}"
sed -i '' "s/android:name=\".*.permission.C2D_MESSAGE\"/android:name=\"${PACKAGE_NAME}.permission.C2D_MESSAGE\"/g" "${MANIFEST_FILE}"

sed -i '' "s/android:versionName=\".*\"/android:versionName=\"${VERSION}\"/g" "${MANIFEST_FILE}"
sed -i '' "s/android:versionCode=\".*\"/android:versionCode=\"${VERSION_CODE}\"/g" "${MANIFEST_FILE}"

sed -i '' "s/android:name=\".*.permission.MAPS_RECEIVE\"/android:name=\"${PACKAGE_NAME}.permission.MAPS_RECEIVE\"/g" "${MANIFEST_FILE}"

sed -i '' "1,/category android/s/category android:name=\".*\"/category android:name=\"${PACKAGE_NAME}\"/" "${MANIFEST_FILE}"
sed -i '' "s/android:name=\".*.AppActivity\"/android:name=\"${PACKAGE_NAME}.AppActivity\"/g" "${MANIFEST_FILE}"

# Replace content of java files import com.mypinkpal.app.R
for file in $(find "${SRC_PATH}" -name "*.java")
do
    echo "Processing file ${file}"
    sed -i '' "s/import .*.R;/import ${PACKAGE_NAME}.R;/g" "${file}"
done