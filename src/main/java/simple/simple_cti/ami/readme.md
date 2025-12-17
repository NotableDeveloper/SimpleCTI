# `AmiConnectionManager`

`AmiConnectionManager` 클래스는 Asterisk Manager Interface (AMI)에 대한 연결을 관리하는 Spring `@Component`입니다.

## 주요 기능
*   **연결 관리:** Asterisk 서버의 AMI에 대한 영구적인 연결을 설정하고 유지합니다.
*   **생명주기 통합:** Spring 애플리케이션 시작 시 (`@PostConstruct`) 자동으로 AMI에 연결하고, 애플리케이션 종료 시 (`@PreDestroy`) 정상적으로 연결을 해제합니다.
*   **설정:** Spring의 `@Value` 어노테이션을 활용하여 `application.properties` (또는 유사한 설정 파일)에서 Asterisk 연결 세부 정보(호스트, 사용자 이름, 비밀번호)를 주입받습니다.
*   **연결 제공자:** 다른 컴포넌트(예: `OutboundCallCommand`, `InboundCallCommand`)가 Asterisk로 명령을 전송하기 위해 활성 AMI 연결에 접근할 수 있도록 `getManagerConnection()` 메서드를 제공합니다.
*   **오류 처리:** 연결 실패 시 로깅 및 애플리케이션 종료 메커니즘을 포함합니다.

---

# `OutboundCallCommand`

`OutboundCallCommand` 클래스는 Spring `@Component`로, Asterisk Manager Interface (AMI)를 통해 외부로 발신 통화를 시작하는 역할을 담당합니다.

## 주요 기능
*   **발신 통화 시작:** 특정 대상 번호(`targetNumber`)로 `OriginateAction`을 생성하여 Asterisk에 전송함으로써 외부 통화를 시작합니다.
*   **설정 주입:** `@Value` 어노테이션을 사용하여 `application.properties`와 같은 설정 파일로부터 발신 관련 정보(채널, 게이트웨이, 컨텍스트, 주 발신번호, 계정 코드)를 주입받습니다.
*   **비동기 처리:** `OriginateAction.setAsync(true)`를 설정하여, Asterisk가 발신 명령을 즉시 처리하고 통화 연결은 백그라운드에서 비동기적으로 진행되도록 합니다. 이를 통해 Java 애플리케이션은 통화 연결을 기다리지 않고 다음 작업을 수행할 수 있습니다.
*   **AMI 연결 활용:** `AmiConnectionManager`를 통해 얻은 AMI 연결을 사용하여 Asterisk와 통신합니다.
*   **오류 처리:** AMI 연결 상태를 확인하고, 연결되지 않은 경우 예외를 발생시켜 오류를 처리합니다.
*   **고객-상담사 브릿지 지원:** `OriginateAction`에 `setAccount(Account)`를 통해 상담사 내선 번호를 전달하며, 이는 Asterisk Dialplan에서 고객이 전화를 받은 후 상담사에게 브릿지하는 데 사용됩니다.

### 참고 - 발신 시에 사용하는 Dialplan (extensions.conf)

```asterisk
[outbound_sample]
exten => _X.,1,NoOp(Client Answered. Bridging to Agent: ${CHANNEL(accountcode)})
same => n,Dial(PJSIP/${CHANNEL(accountcode)},30)
same => n,Hangup()
```
