-- Simple-CTI MariaDB 초기화 스키마
-- Phase 1: CDR (통화 이력) 테이블

CREATE TABLE IF NOT EXISTS cdr (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    accountcode     VARCHAR(20)     DEFAULT '' COMMENT '계정 코드',
    src             VARCHAR(80)     NOT NULL COMMENT '발신번호',
    dst             VARCHAR(80)     NOT NULL COMMENT '수신번호',
    dcontext        VARCHAR(80)     DEFAULT '' COMMENT '다이얼플랜 컨텍스트',
    clid            VARCHAR(80)     DEFAULT '' COMMENT '발신자 ID',
    channel         VARCHAR(80)     DEFAULT '' COMMENT '에이전트 채널',
    dstchannel      VARCHAR(80)     DEFAULT '' COMMENT '고객 채널',
    lastapp         VARCHAR(80)     DEFAULT '' COMMENT '마지막 실행 앱',
    lastdata        VARCHAR(80)     DEFAULT '' COMMENT '마지막 앱 데이터',
    calldate        DATETIME        NOT NULL COMMENT '통화 시작 시각',
    answer          DATETIME        NULL COMMENT '응답 시각',
    end             DATETIME        NULL COMMENT '종료 시각',
    duration        INT             DEFAULT 0 COMMENT '총 시간(초): 발신~종료',
    billsec         INT             DEFAULT 0 COMMENT '과금 시간(초): 응답~종료',
    disposition     VARCHAR(45)     DEFAULT '' COMMENT 'ANSWERED/NO ANSWER/BUSY/FAILED',
    amaflags        VARCHAR(45)     DEFAULT '' COMMENT 'AMA 플래그',
    uniqueid        VARCHAR(32)     NOT NULL UNIQUE COMMENT 'Asterisk 고유 통화 ID',
    userfield       VARCHAR(255)    DEFAULT '',
    recording_file  VARCHAR(255)    DEFAULT NULL COMMENT '녹음 파일명 (없으면 NULL)',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_calldate   (calldate),
    INDEX idx_src        (src),
    INDEX idx_dst        (dst),
    INDEX idx_disposition (disposition)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='통화 이력';
