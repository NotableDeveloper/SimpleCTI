# Simple-CTI

A web-based Computer Telephony Integration (CTI) system built with Spring Boot and Vue.js.

## Branches

| Branch | Directory | Description |
|--------|-----------|-------------|
| `main` | `main/` | Project overview and documentation |
| `app` | `application/` | Spring Boot backend (AMI, REST API) |
| `softphone` | `softphone/` | Vue.js frontend (dialer UI, recordings) |
| `infra` | `infra/` | Infrastructure configuration (Docker Compose) |

## Tech Stack

- **Backend**: Java 17, Spring Boot 3, asterisk-java
- **Frontend**: Vue.js 3, sip.js (WebRTC)
- **Infrastructure**: Docker, Docker Compose
- **PBX**: Asterisk 20
