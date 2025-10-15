#!/bin/bash
# Android SDK Installation Script for Dev Container

set -e

echo "🤖 Installing Android SDK..."

# Create SDK directory
SDK_DIR="$HOME/android-sdk"
mkdir -p "$SDK_DIR"

# Download command line tools
echo "📥 Downloading Android command line tools..."
cd /tmp
wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
unzip -q commandlinetools-linux-9477386_latest.zip

# Setup command line tools
mkdir -p "$SDK_DIR/cmdline-tools/latest"
mv cmdline-tools/* "$SDK_DIR/cmdline-tools/latest/" 2>/dev/null || true

# Set environment variables
export ANDROID_HOME="$SDK_DIR"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"

# Accept licenses
yes | "$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" --licenses || true

# Install required SDK components
echo "📦 Installing SDK components..."
"$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" \
    "platform-tools" \
    "platforms;android-34" \
    "build-tools;34.0.0" \
    "extras;google;google_play_services" \
    "extras;android;m2repository" \
    "extras;google;m2repository"

# Add to bash profile
echo "" >> ~/.bashrc
echo "# Android SDK" >> ~/.bashrc
echo "export ANDROID_HOME=\"$SDK_DIR\"" >> ~/.bashrc
echo "export PATH=\"\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools\"" >> ~/.bashrc

# Create local.properties
echo "sdk.dir=$SDK_DIR" > /workspaces/coffeerankingapk/local.properties

echo "✅ Android SDK installed successfully!"
echo "📍 SDK Location: $SDK_DIR"
echo ""
echo "⚠️  Please run: source ~/.bashrc"
echo "Then try building again: ./gradlew assembleDebug"
