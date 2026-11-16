# Azimuth — Development Rules

## Core Rule
**Before writing any code, ALWAYS verify source data first:**
- Check database schema (backend models)
- Check API contracts (openapi.yaml, schemas)
- Check backend endpoints and their responses
- Ensure mobile app models match backend contracts

## Architecture
- Clean Architecture + MVVM
- Single app module with packages (no multi-modularity)
- Packages: data, domain, presentation

## Tech Stack
- Kotlin, Jetpack Compose
- Retrofit, Hilt, Room, DataStore

## Git
- Branch: `feature`
- Commit messages: conventional (feat:, fix:, refactor:, etc.)
