# Change Log

All notable changes to this project will be documented in this file. This change log follows the conventions
of [keepachangelog.com](http://keepachangelog.com/).

## 0.100.302 - 2026-03-29

### Fixed

- `+component` flags now switch to opt-in mode (all components disabled by default, only specified ones enabled); `-component` flags retain opt-out mode.

## 0.100.301 - 2026-03-29

### Added

- Optional component selection via `+/-` profile flags (`datalevin`, `http-server`, `telegram`, `http-client`); all
  components included by default.

