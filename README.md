# LTE - LibreTube Local Edition

A fully local-only Android client for YouTube, built on modern Android technologies. This is a standalone fork of LibreTube that removes all Piped instance dependencies in favor of a completely offline, device-centric architecture.

## 🎯 Core Philosophy

**Local-First, Privacy-First**: No accounts, no servers, no tracking. Everything stays on your device.

## ✨ Key Features

### 🚀 Modern Architecture
- **Jetpack Compose Player**: Video player completely rewritten using Jetpack Compose, `AnchorDraggable`, and `Media3`/`ExoPlayer` for smooth, gesture-driven playback
- **Room Database**: All subscriptions, playlists, and watch history stored locally on your device
- **Material 3 UI**: Modern Material Design 3 with dynamic colors and adaptive components
- **English Only**: Streamlined single-language build for reduced complexity and APK size

### 📺 Enhanced Playback
- **DeArrow Integration**: Crowdsourced titles and thumbnails to combat clickbait
- **SponsorBlock**: Automatic skipping of sponsored segments and other unwanted content
- **Audio/Video Toggle**: Switch between video and audio-only modes on the fly
- **Background Playback**: Full background play support with media notification controls
- **Picture-in-Picture**: Seamless PiP mode for multitasking

### 🛠️ Power User Features
- **In-App Log Viewer**: Real-time debugging and diagnostics
- **Automatic Backups**: Daily database backups with configurable retention
- **Custom Playlist Ordering**: Drag-and-drop reordering for local playlists
- **Download Manager**: Built-in video/audio download functionality
- **Self-Updating**: Automatic update checks from this repository

## 📦 Installation

Download the latest APK from the [Releases](https://github.com/akashsriramganapathy/lte/releases) page.

## 🔧 Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + XML Views (hybrid)
- **Database**: Room (SQLite)
- **Media Playback**: Media3 / ExoPlayer
- **Video Extraction**: NewPipe Extractor
- **Build System**: Gradle with Kotlin DSL

## 📁 Repository

**GitHub**: [https://github.com/akashsriramganapathy/lte](https://github.com/akashsriramganapathy/lte)

## 🙏 Credits

- **Original Project**: Forked from [LibreTube](https://github.com/libre-tube/LibreTube)
- **Development**: Built with AI-assisted development workflows
- **Community**: Thanks to NewPipe, SponsorBlock, and DeArrow projects

## ⚠️ Disclaimer

This is an experimental fork focused on local-only functionality. It is not affiliated with or endorsed by the LibreTube project.

## 📄 License

GNU General Public License v3.0 - See [LICENSE](LICENSE) for details.