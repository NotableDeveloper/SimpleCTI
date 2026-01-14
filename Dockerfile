FROM node:lts-alpine

# 작업 디렉토리 설정
WORKDIR /app

# npm install을 위해 package.json과 package-lock.json만 먼저 복사 (캐시 활용)
# 바인드 마운트를 사용하더라도, 이미지를 빌드할 때 의존성을 설치해두면
# 컨테이너 시작 시 node_modules 볼륨이 비어있을 때 유용할 수 있음.
# 하지만 "매번 이미지를 빌드하고 싶지 않다"는 요구사항에 맞춰
# 실행 시점에 의존성이 없으면 설치하도록 entrypoint를 구성하는 것이 좋을 수도 있으나,
# 가장 단순한 형태인 "실행 환경"만 제공합니다.

EXPOSE 8080

# 기본 실행 명령어 (개발 서버)
# 호스트의 소스코드가 바인드 마운트되므로, 컨테이너 내부에서 npm install 후 실행해야 할 수 있음.
CMD ["npm", "run", "serve"]
