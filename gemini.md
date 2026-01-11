# 프로젝트 소개
이 프로젝트는 Asterisk를 기반으로, 사용자와 웹으로 상호작용하는 CTI 서비스입니다. 이 서비스를 통해 사용자는
웹 페이지 안에서 전화발신, ARS 설정, 통화 내역 등을 조회할 수 있게 됩니다.

---

## 패키지 요약
패키자마다 구현 내용이 궁금하다면, 해당 패키지에 이동하여 gemini.md 파일의 내용을 숙지해주세요.

### `simple.simple_cti.ami`
이 패키지는 Asterisk Manager Interface(AMI)와의 통신을 담당합니다.

*   **`AmiConnectionManager.java`**: Spring 애플리케이션의 생명주기에 맞춰 Asterisk AMI에 대한 연결을 설정, 관리 및 해제합니다. 연결 정보는 `application.properties`에서 가져오며, 다른 컴포넌트가 AMI 연결을 사용할 수 있도록 제공합니다. 연결 실패 시 애플리케이션을 종료하는 오류 처리 로직을 포함합니다.
*   **`OutboundCallCommand.java`**: AMI를 통해 외부로 전화를 발신하는 `Originate` 명령을 실행합니다. 기존 고객 우선 발신 방식에서 **상담원(Agent) 우선 발신** 방식으로 변경되었습니다. 상담원과 먼저 연결된 후 `Dial` 애플리케이션을 통해 고객에게 연결함으로써, 초기 RTP 패킷 미수신으로 인한 15초 타임아웃 및 음성 미송출 문제를 해결합니다.

### `simple.simple_cti.controller`
이 패키지는 웹 요청을 처리하는 Spring MVC 컨트롤러를 포함합니다.

*   **`HomeController.java`**: 웹 UI의 메인 페이지와 다이얼 페이지를 제공합니다. 메인 페이지('/') 요청 시, AMI 연결 상태, Asterisk 버전 등의 정보를 모델에 담아 `home.html` 템플릿으로 전달합니다. `/dial` 요청 시에는 `dial.html`을 렌더링합니다.
*   **`DialController.java`**: 전화 발신 요청을 처리하는 REST 컨트롤러입니다. `/originate` 엔드포인트에 대한 POST 요청을 받아, `targetNumber` 파라미터를 사용하여 `OutboundCallCommand`를 실행하고, 그 결과를 JSON 형태로 클라이언트에 반환합니다.
