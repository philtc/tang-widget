#!/usr/bin/env sh
set -x

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/bin/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/bin/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME\n\nPlease set the JAVA_HOME environment variable in your environment to match the location of your Java installation."
        exit 1
    fi
else
    JAVACMD="java"
fi

APP_HOME="$(
  cd "$(dirname "$0")" || exit
  pwd -P
)"

# Add default JVM options here. You can also use the JAVA_OPTS environment variable to pass JVM options to this script.
DEFAULT_JVM_OPTS=""

# Determine the script name.
SCRIPT_NAME="$(basename "$0")"

# Determine the path to the wrapper jar.
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_PROPERTIES="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

# If the wrapper jar doesn't exist, download it.
if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading Gradle distribution..."

    # Extract distributionUrl from gradle-wrapper.properties
    DISTRIBUTION_URL=$(grep 'distributionUrl' "$WRAPPER_PROPERTIES" | sed 's/^distributionUrl=\(.*\)$/\1/' | sed 's///g')
    DISTRIBUTION_ZIP_NAME=$(basename "$DISTRIBUTION_URL")
    TEMP_DIR="/tmp/gradle_download_$$"
    mkdir -p "$TEMP_DIR"
    TEMP_ZIP="$TEMP_DIR/$DISTRIBUTION_ZIP_NAME"

    echo "Downloading $DISTRIBUTION_URL to $TEMP_ZIP"
    wget -q "$DISTRIBUTION_URL" -O "$TEMP_ZIP"

    if [ $? -ne 0 ]; then
        echo "Error: Failed to download Gradle distribution."
        rm -rf "$TEMP_DIR"
        exit 1
    fi

    echo "Extracting Gradle wrapper JAR..."
    unzip -q "$TEMP_ZIP" -d "$TEMP_DIR"

    # Find the actual gradle-wrapper.jar inside the extracted directory
    # It's usually in a subdirectory like gradle-X.Y/lib/gradle-wrapper-X.Y.jar
    FOUND_WRAPPER_JAR=$(find "$TEMP_DIR" -name "gradle-wrapper-*.jar" | head -n 1)

    if [ -z "$FOUND_WRAPPER_JAR" ]; then
        echo "Error: Could not find gradle-wrapper.jar in the downloaded distribution."
        rm -rf "$TEMP_DIR"
        exit 1
    fi

    cp "$FOUND_WRAPPER_JAR" "$WRAPPER_JAR"

    if [ $? -ne 0 ]; then
        echo "Error: Failed to copy gradle-wrapper.jar."
        rm -rf "$TEMP_DIR"
        exit 1
    fi

    echo "Gradle wrapper downloaded and installed."
    rm -rf "$TEMP_DIR"
fi

# Execute Gradle.
exec "$JAVACMD" $DEFAULT_JVM_OPTS -Dorg.gradle.appname="$SCRIPT_NAME" -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"