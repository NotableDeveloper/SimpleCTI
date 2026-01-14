# Simple CTI Softphone

Simple CTI 시스템의 프론트엔드 웹 애플리케이션입니다. Vue.js 3를 사용하여 구축되었으며, 상담원에게 다이얼패드 인터페이스와 시스템 상태 정보를 제공합니다.

## 주요 기능

- **시스템 상태 모니터링**: Application Server 및 Asterisk Connection 상태를 실시간(30초 주기)으로 확인합니다.
- **아웃바운드 콜 (Click-to-Dial)**: 다이얼패드를 통해 전화번호를 입력하고 Asterisk를 통한 호 시도를 수행합니다.
- **녹음 설정**: 통화 시작 전 녹음 여부를 선택할 수 있습니다.
- **녹음 파일 관리**: 서버에 저장된 녹음 파일 목록을 확인하고 개별 또는 일괄 다운로드할 수 있습니다.

## 기술 스택

- **Framework**: Vue.js 3 (Options API)
- **Build Tool**: Vue CLI 5
- **HTTP Client**: Native Fetch API

## 프로젝트 설정 및 실행

### 설치
```bash
npm install
```

### 개발 서버 실행
```bash
npm run serve
```

### 프로덕션 빌드
```bash
npm run build
```

### 코드 스타일 체크
```bash
npm run lint
```

## 백엔드 연동
이 애플리케이션은 아웃바운드 호출 및 상태 확인을 위해 `application` 모듈(Spring Boot)에서 제공하는 API를 사용합니다. API 엔드포인트는 `vue.config.js` 또는 백엔드 서버 설정에 따라 프록시될 수 있습니다.