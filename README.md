# SimpleCTI

SimpleCTI는 Spring Boot와 Asterisk를 연동하여 웹 기반 CTI(Computer Telephony Integration) 기능을 제공하는 프로젝트입니다. 사용자는 웹 화면을 통해 전화를 걸 수 있습니다.

## 주요 기능

- **Asterisk Manager Interface (AMI) 연동**: Spring 애플리케이션 시작 시 자동으로 Asterisk에 연결하고, 안전하게 연결을 종료합니다.
- **웹 기반 전화 발신**: 웹 페이지에서 전화번호를 입력하여 지정된 번호로 전화를 발신하는 기능을 제공합니다.

## 시스템 아키텍처 및 동작 방식

### 1. AMI (Asterisk Manager Interface) 연결

- **`AmiConnectionManager.java`**
  - Spring의 `@Component`로 등록되어 애플리케이션이 시작될 때 (`@PostConstruct`) 자동으로 Asterisk Manager Interface에 로그인합니다.
  - `application.properties` 파일에 정의된 `asterisk.host`, `asterisk.username`, `asterisk.password` 값을 사용하여 연결을 설정합니다.
  - 연결이 실패하면 애플리케이션은 즉시 종료되어 불안정한 상태를 방지합니다.
  - 애플리케이션이 종료될 때 (`@PreDestroy`) Asterisk에서 로그오프하여 연결을 안전하게 해제합니다.
  - 생성된 `ManagerConnection` 객체는 다른 컴포넌트에서 재사용할 수 있도록 Singleton으로 관리됩니다.

### 2. 웹을 통한 외부 발신

이 기능은 사용자가 웹 UI에서 전화를 거는 과정을 처리합니다.

- **흐름**: `Web UI` -> `DialController` -> `OutboundCallCommand` -> `AmiConnectionManager` -> `Asterisk`

1.  **사용자 요청**: 사용자가 `dial.html` 페이지에서 발신할 전화번호(`targetNumber`)를 입력하고 '전화 걸기' 버튼을 클릭합니다.
2.  **Controller 수신**: JavaScript는 이 요청을 `POST /originate` API로 전송합니다. `DialController`는 이 요청을 받아 `targetNumber`를 파라미터로 추출합니다.
3.  **발신 명령 생성**: `DialController`는 `OutboundCallCommand`의 `executeOriginate` 메서드를 호출합니다.
4.  **AMI 액션 전송**: `OutboundCallCommand`는 `OriginateAction`을 생성합니다. 이때 발신에 필요한 채널, 컨텍스트, 발신자 정보 등은 `application.properties`의 `outbound.*` 설정값을 사용합니다.
5.  **Asterisk 실행**: 생성된 액션은 `AmiConnectionManager`를 통해 Asterisk 서버로 전송되고, Asterisk는 설정된 값에 따라 실제 전화 발신을 수행합니다.
6.  **응답**: `DialController`는 발신 요청의 성공 또는 실패 여부를 JSON 형식으로 웹 UI에 반환합니다.

## 설정

프로젝트를 실행하기 전에 `src/main/resources/application.properties` 파일에 Asterisk 접속 및 발신 관련 정보를 설정해야 합니다.

```properties
# Asterisk AMI Connection
asterisk.host=your_asterisk_host
asterisk.username=your_ami_username
asterisk.password=your_ami_password

# Outbound Call Configuration
outbound.channel=PJSIP/
outbound.gateway=@your_gateway
outbound.context=outbound_sample
outbound.main_number=your_main_caller_id
outbound.account=agent_extension # 상담사 내선번호
```

## 실행 방법

1.  프로젝트를 클론하고 의존성을 다운로드합니다.
2.  `application.properties` 파일을 위 설명에 따라 수정합니다.
3.  아래 명령어를 사용하여 애플리케이션을 실행합니다.

    ```shell
    ./gradlew bootRun
    ```

4.  웹 브라우저에서 `http://localhost:8080`에 접속하여 상태를 확인하고, `http://localhost:8080/dial`에서 전화 발신 기능을 사용할 수 있습니다.
