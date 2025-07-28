#!/bin/bash

# Super Smash Mobs Brawl - Code Formatting Script
# This script automatically formats all Kotlin code in the project

set -e

echo "🎨 Formatting Super Smash Mobs Brawl codebase..."

# Change to the plugin directory
cd "$(dirname "$0")/.." || exit 1

echo "📋 Running ktfmt formatter..."
./gradlew ktfmtFormat --no-daemon

echo "🔧 Running ktlint formatter..."
./gradlew ktlintFormat --no-daemon

echo "✅ Code formatting complete!"
echo ""
echo "🧪 Running code quality checks..."
./gradlew codeQuality --no-daemon

echo "🎉 All formatting and quality checks passed!"