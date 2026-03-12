<template>
  <div id="main-app">

    <!-- ==================== 사이드바 ==================== -->
    <aside class="sidebar">
      <div class="sidebar-logo">
        <a href="#" class="sidebar-logo-mark">
          <div class="logo-icon">
            <!-- 전화 아이콘 -->
            <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path d="M6.6 10.8c1.4 2.8 3.8 5.1 6.6 6.6l2.2-2.2c.3-.3.7-.4 1-.2 1.1.4 2.3.6 3.6.6.6 0 1 .4 1 1V20c0 .6-.4 1-1 1-9.4 0-17-7.6-17-17 0-.6.4-1 1-1h3.5c.6 0 1 .4 1 1 0 1.3.2 2.5.6 3.6.1.3 0 .7-.2 1L6.6 10.8z"/>
            </svg>
          </div>
          <div class="logo-text">
            <span class="logo-title">Simple CTI</span>
            <span class="logo-subtitle">Admin Console</span>
          </div>
        </a>
      </div>

      <nav class="sidebar-nav">
        <span class="nav-section-label">Overview</span>

        <!-- 대시보드 네비게이션 항목 -->
        <button
          type="button"
          class="nav-item"
          :class="{ active: currentView === 'status' }"
          @click="currentView = 'status'"
        >
          <span class="nav-icon">
            <!-- 대시보드 아이콘 -->
            <svg viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
              <rect x="3" y="3" width="7" height="7" rx="1.5"/>
              <rect x="14" y="3" width="7" height="7" rx="1.5"/>
              <rect x="3" y="14" width="7" height="7" rx="1.5"/>
              <rect x="14" y="14" width="7" height="7" rx="1.5"/>
            </svg>
          </span>
          <span>대시보드</span>
        </button>

        <span class="nav-section-label">기능</span>

        <!-- 수동 콜 네비게이션 항목 -->
        <button
          type="button"
          class="nav-item"
          :class="{ active: currentView === 'dial' }"
          @click="currentView = 'dial'"
        >
          <span class="nav-icon">
            <!-- 다이얼러 아이콘 -->
            <svg viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
              <path d="M6.6 10.8c1.4 2.8 3.8 5.1 6.6 6.6l2.2-2.2c.3-.3.7-.4 1-.2 1.1.4 2.3.6 3.6.6.6 0 1 .4 1 1V20c0 .6-.4 1-1 1-9.4 0-17-7.6-17-17 0-.6.4-1 1-1h3.5c.6 0 1 .4 1 1 0 1.3.2 2.5.6 3.6.1.3 0 .7-.2 1L6.6 10.8z"/>
            </svg>
          </span>
          <span>수동 콜</span>
        </button>

        <!-- 녹음 파일 네비게이션 항목 -->
        <button
          type="button"
          class="nav-item"
          :class="{ active: currentView === 'recordings' }"
          @click="currentView = 'recordings'"
        >
          <span class="nav-icon">
            <!-- 녹음 아이콘 -->
            <svg viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 15c1.66 0 3-1.34 3-3V6c0-1.66-1.34-3-3-3S9 4.34 9 6v6c0 1.66 1.34 3 3 3zm5.91-3c-.49 0-.9.36-.98.85C16.52 15.2 14.47 17 12 17s-4.52-1.8-4.93-4.15c-.08-.49-.49-.85-.98-.85-.61 0-1.09.54-1 1.14.49 3 2.89 5.35 5.91 5.78V21c0 .55.45 1 1 1s1-.45 1-1v-2.08c3.02-.43 5.42-2.78 5.91-5.78.1-.6-.39-1.14-1-1.14z"/>
            </svg>
          </span>
          <span>녹음 파일</span>
        </button>
      </nav>

      <div class="sidebar-footer">
        <div class="sidebar-version">v1.0.0</div>
      </div>
    </aside>

    <!-- ==================== 메인 콘텐츠 영역 ==================== -->
    <div class="main-content">

      <!-- 상단 바 -->
      <header class="topbar">
        <div class="topbar-title">
          <span class="topbar-heading">{{ currentView === 'status' ? '시스템 대시보드' : (currentView === 'dial' ? '수동 콜' : '녹음 파일 관리') }}</span>
          <span class="topbar-breadcrumb">Simple CTI &rsaquo; {{ currentView === 'status' ? 'Dashboard' : (currentView === 'dial' ? 'Dialer' : 'Recordings') }}</span>
        </div>
        <div class="topbar-actions">
          <!-- 새로고침 버튼: checkHealth 메서드 호출 -->
          <button class="refresh-btn" @click="checkHealth">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 4 23 10 17 10"/>
              <polyline points="1 20 1 14 7 14"/>
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
            </svg>
            새로고침
          </button>
        </div>
      </header>

      <!-- 대시보드 뷰 -->
      <main v-if="currentView === 'status'" class="page-body">

        <!-- 요약 스트립 -->
        <div class="summary-strip">
          <!-- Application Server 상태 chip -->
          <div class="summary-chip">
            <span
              class="chip-dot"
              :class="appStatus.status === 'UP' ? 'success' : (appStatus.status === 'CHECKING...' ? 'warning' : 'error')"
            ></span>
            <span class="chip-label">Application Server</span>
            <span>{{ appStatus.status }}</span>
          </div>
          <!-- Asterisk 상태 chip -->
          <div class="summary-chip">
            <span
              class="chip-dot"
              :class="asteriskStatus.connectionState === 'CONNECTED' ? 'success' : (asteriskStatus.connectionState === 'UNKNOWN' ? 'warning' : 'error')"
            ></span>
            <span class="chip-label">Asterisk</span>
            <span>{{ asteriskStatus.connectionState }}</span>
          </div>
          <!-- Last Updated -->
          <div class="last-updated-bar">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            Last Updated:&nbsp;<span class="time-value">{{ formatTime(lastUpdated) || '--:--:--' }}</span>
          </div>
        </div>

        <!-- 연결 상태 섹션 헤더 -->
        <div class="section-header">
          <span class="section-title">연결 상태</span>
          <span class="section-count">2개 서비스</span>
        </div>

        <!-- 상태 카드 그리드 -->
        <div class="cards-grid">

          <!-- Application Server 카드 -->
          <div class="status-card">
            <div class="card-header">
              <div class="card-header-left">
                <div class="card-icon-wrap blue">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="2" y="3" width="20" height="14" rx="2"/>
                    <line x1="8" y1="21" x2="16" y2="21"/>
                    <line x1="12" y1="17" x2="12" y2="21"/>
                  </svg>
                </div>
                <div class="card-title-group">
                  <span class="card-title">Application Server</span>
                  <span class="card-subtitle">Spring Boot Backend</span>
                </div>
              </div>
              <!-- 상태 배지: getStatusClass 메서드로 동적 클래스 바인딩 -->
              <span class="status-badge" :class="getStatusClass(appStatus.status)">
                <span class="badge-dot" :class="appStatus.status === 'UP' ? 'pulse' : ''"></span>
                {{ appStatus.status }}
              </span>
            </div>
            <div class="card-body">
              <div class="card-row">
                <span class="card-row-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
                  </svg>
                  Port
                </span>
                <span class="card-row-value mono">:8090</span>
              </div>
              <div class="card-divider"></div>
              <div class="card-row">
                <span class="card-row-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="16 18 22 12 16 6"/>
                    <polyline points="8 6 2 12 8 18"/>
                  </svg>
                  Status
                </span>
                <span class="card-row-value mono">{{ appStatus.status === 'UP' ? '200 OK' : appStatus.status }}</span>
              </div>
            </div>
          </div>

          <!-- Asterisk Connection 카드 -->
          <div class="status-card">
            <div class="card-header">
              <div class="card-header-left">
                <div class="card-icon-wrap purple">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M6.6 10.8c1.4 2.8 3.8 5.1 6.6 6.6l2.2-2.2c.3-.3.7-.4 1-.2 1.1.4 2.3.6 3.6.6.6 0 1 .4 1 1V20c0 .6-.4 1-1 1-9.4 0-17-7.6-17-17 0-.6.4-1 1-1h3.5c.6 0 1 .4 1 1 0 1.3.2 2.5.6 3.6.1.3 0 .7-.2 1L6.6 10.8z"/>
                  </svg>
                </div>
                <div class="card-title-group">
                  <span class="card-title">Asterisk Connection</span>
                  <span class="card-subtitle">PBX Server (AMI)</span>
                </div>
              </div>
              <!-- 연결 상태 배지: getConnectionClass 메서드로 동적 클래스 바인딩 -->
              <span class="status-badge" :class="getConnectionClass(asteriskStatus.connectionState)">
                <span class="badge-dot" :class="asteriskStatus.connectionState === 'CONNECTED' ? 'pulse' : ''"></span>
                {{ asteriskStatus.connectionState }}
              </span>
            </div>
            <div class="card-body">
              <div class="card-row">
                <span class="card-row-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="2" y="2" width="20" height="8" rx="2"/>
                    <rect x="2" y="14" width="20" height="8" rx="2"/>
                    <line x1="6" y1="6" x2="6.01" y2="6"/>
                    <line x1="6" y1="18" x2="6.01" y2="18"/>
                  </svg>
                  Host
                </span>
                <span class="card-row-value mono">{{ asteriskStatus.host }}</span>
              </div>
              <div class="card-divider"></div>
              <div class="card-row">
                <span class="card-row-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                  User
                </span>
                <span class="card-row-value mono">{{ asteriskStatus.username }}</span>
              </div>
              <div class="card-row">
                <span class="card-row-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="3"/>
                    <path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4M4.22 19.78l2.83-2.83M16.95 7.05l2.83-2.83"/>
                  </svg>
                  Version
                </span>
                <span class="card-row-value mono">{{ asteriskStatus.asteriskVersion }}</span>
              </div>
              <div class="card-divider"></div>
              <div class="card-row">
                <span class="card-row-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M1 6v16l7-4 8 4 7-4V2l-7 4-8-4-7 4z"/>
                    <line x1="8" y1="2" x2="8" y2="18"/>
                    <line x1="16" y1="6" x2="16" y2="22"/>
                  </svg>
                  State
                </span>
                <span class="status-badge" :class="getConnectionClass(asteriskStatus.connectionState)" style="font-size: 11px; padding: 3px 8px;">
                  <span class="badge-dot" :class="asteriskStatus.connectionState === 'CONNECTED' ? 'pulse' : ''"></span>
                  {{ asteriskStatus.connectionState }}
                </span>
              </div>
            </div>
          </div>

        </div>

        <!-- 빠른 이동 섹션 -->
        <div class="quick-action-section">
          <div class="section-header">
            <span class="section-title">빠른 이동</span>
          </div>
          <div class="action-cards-row">

            <!-- 수동 콜 빠른 이동 카드 -->
            <button type="button" class="action-card" @click="currentView = 'dial'">
              <div class="action-card-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M6.6 10.8c1.4 2.8 3.8 5.1 6.6 6.6l2.2-2.2c.3-.3.7-.4 1-.2 1.1.4 2.3.6 3.6.6.6 0 1 .4 1 1V20c0 .6-.4 1-1 1-9.4 0-17-7.6-17-17 0-.6.4-1 1-1h3.5c.6 0 1 .4 1 1 0 1.3.2 2.5.6 3.6.1.3 0 .7-.2 1L6.6 10.8z"/>
                </svg>
              </div>
              <div class="action-card-text">
                <div class="action-card-title">수동 콜 (다이얼러)</div>
                <div class="action-card-desc">번호를 입력하여 발신 통화를 시작합니다</div>
              </div>
              <div class="action-card-arrow">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="9 18 15 12 9 6"/>
                </svg>
              </div>
            </button>

            <!-- 녹음 파일 관리 빠른 이동 카드 -->
            <button type="button" class="action-card" @click="currentView = 'recordings'">
              <div class="action-card-icon green-bg">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 15c1.66 0 3-1.34 3-3V6c0-1.66-1.34-3-3-3S9 4.34 9 6v6c0 1.66 1.34 3 3 3zm5.91-3c-.49 0-.9.36-.98.85C16.52 15.2 14.47 17 12 17s-4.52-1.8-4.93-4.15c-.08-.49-.49-.85-.98-.85-.61 0-1.09.54-1 1.14.49 3 2.89 5.35 5.91 5.78V21c0 .55.45 1 1 1s1-.45 1-1v-2.08c3.02-.43 5.42-2.78 5.91-5.78.1-.6-.39-1.14-1-1.14z"/>
                </svg>
              </div>
              <div class="action-card-text">
                <div class="action-card-title">녹음 파일 관리</div>
                <div class="action-card-desc">저장된 통화 녹음 파일을 조회하고 다운로드합니다</div>
              </div>
              <div class="action-card-arrow">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="9 18 15 12 9 6"/>
                </svg>
              </div>
            </button>

          </div>
        </div>

      </main>

      <!-- 수동 콜(Dialer) 뷰: 사이드바와 함께 main-content 내에 렌더링 -->
      <main v-if="currentView === 'dial'" class="page-body page-body--dialer">
        <Dialer @back="currentView = 'status'" />
      </main>

      <!-- 녹음 파일 관리(Recordings) 뷰: 사이드바와 함께 main-content 내에 렌더링 -->
      <main v-if="currentView === 'recordings'" class="page-body page-body--recordings">
        <Recordings @back="currentView = 'status'" />
      </main>

    </div>
    <!-- /.main-content -->

  </div>
</template>

<script>
import Dialer from './components/Dialer.vue'
import Recordings from './components/Recordings.vue'

export default {
  name: 'App',
  components: {
    Dialer,
    Recordings
  },
  data() {
    return {
      currentView: 'status', // 'status' | 'dial' | 'recordings'
      refreshInterval: null,
      lastUpdated: null,
      appStatus: {
        status: 'CHECKING...',
        timestamp: null
      },
      asteriskStatus: {
        host: 'Loading...',
        username: 'Loading...',
        connectionState: 'UNKNOWN',
        asteriskVersion: 'Loading...'
      }
    }
  },
  mounted() {
    this.checkHealth();
    // 30초마다 헬스 체크 반복
    this.refreshInterval = setInterval(this.checkHealth, 30000);
  },
  beforeUnmount() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  },
  methods: {
    async checkHealth() {
      await Promise.all([
        this.fetchAppHealth(),
        this.fetchAsteriskStatus()
      ]);
      this.lastUpdated = new Date();
    },
    async fetchAppHealth() {
      try {
        const response = await fetch(`${process.env.VUE_APP_API_BASE_URL}/api/health`);
        if (response.ok) {
          const data = await response.json();
          this.appStatus = data;
        } else {
          this.appStatus.status = 'DOWN';
        }
      } catch (error) {
        console.error('Error fetching app health:', error);
        this.appStatus.status = 'OFFLINE';
      }
    },
    async fetchAsteriskStatus() {
      try {
        const response = await fetch(`${process.env.VUE_APP_API_BASE_URL}/api/health/asterisk`);
        if (response.ok) {
          const data = await response.json();
          this.asteriskStatus = data;
        } else {
          console.error('Failed to fetch asterisk status');
          this.asteriskStatus.connectionState = 'ERROR';
        }
      } catch (error) {
        console.error('Error fetching asterisk status:', error);
        this.asteriskStatus.connectionState = 'OFFLINE';
      }
    },
    /**
     * Application Server 상태 값에 따라 status-badge 클래스를 반환합니다.
     * @param {string} status - 'UP' | 'DOWN' | 'OFFLINE' | 'CHECKING...'
     */
    getStatusClass(status) {
      if (status === 'UP') return 'badge-success';
      if (status === 'CHECKING...') return 'badge-warning';
      return 'badge-error';
    },
    /**
     * Asterisk 연결 상태 값에 따라 status-badge 클래스를 반환합니다.
     * @param {string} state - 'CONNECTED' | 'UNKNOWN' | 'ERROR' | 'OFFLINE'
     */
    getConnectionClass(state) {
      if (state === 'CONNECTED') return 'badge-success';
      if (state === 'UNKNOWN') return 'badge-warning';
      return 'badge-error';
    },
    /**
     * Date 객체를 HH:MM:SS 형식의 시간 문자열로 변환합니다.
     * @param {Date|null} timestamp
     */
    formatTime(timestamp) {
      if (!timestamp) return '';
      return new Date(timestamp).toLocaleTimeString('en-GB', { hour12: false });
    }
  }
}
</script>

<style>
/*
 * App.vue 레이아웃 스타일: 사이드바, 메인 콘텐츠, 대시보드 UI 전용
 * 전역 CSS 변수 및 reset 스타일은 assets/styles/variables.css에서 관리합니다.
 */

#main-app {
  display: flex;
  width: 100%;
  min-height: 100vh;
}

/* ==================== 사이드바 ==================== */

.sidebar {
  width: var(--sidebar-width);
  min-height: 100vh;
  background: var(--color-sidebar);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.15);
}

.sidebar-logo {
  padding: 24px 20px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.sidebar-logo-mark {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: var(--color-primary);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-icon svg {
  width: 20px;
  height: 20px;
  fill: #ffffff;
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.logo-title {
  font-size: 15px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 0.3px;
}

.logo-subtitle {
  font-size: 10px;
  font-weight: 400;
  color: var(--color-sidebar-text);
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-section-label {
  font-size: 10px;
  font-weight: 600;
  color: var(--color-sidebar-text);
  letter-spacing: 1px;
  text-transform: uppercase;
  padding: 8px 8px 4px;
  margin-top: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  text-decoration: none;
  color: var(--color-sidebar-text);
  font-size: 13.5px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
}

.nav-item:hover:not(:disabled) {
  background: var(--color-sidebar-hover);
  color: #d1d5db;
}

.nav-item.active {
  background: var(--color-sidebar-active);
  color: var(--color-sidebar-text-active);
}

.nav-item.active .nav-icon {
  color: var(--color-primary);
}

/* 비활성화된 nav-item (녹음 파일 — 미구현) */
.nav-item:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.nav-icon {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.nav-icon svg {
  width: 16px;
  height: 16px;
}

.sidebar-footer {
  padding: 16px 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.sidebar-version {
  font-size: 11px;
  color: var(--color-sidebar-text);
  text-align: center;
  opacity: 0.6;
}

/* ==================== 메인 콘텐츠 ==================== */

.main-content {
  margin-left: var(--sidebar-width);
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.topbar {
  background: var(--color-card);
  border-bottom: 1px solid var(--color-border);
  padding: 0 32px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 50;
}

.topbar-title {
  display: flex;
  flex-direction: column;
}

.topbar-heading {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.3;
}

.topbar-breadcrumb {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border-radius: 7px;
  border: 1px solid var(--color-border);
  background: var(--color-card);
  color: var(--color-text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}

.refresh-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: #f0f5ff;
}

.refresh-btn svg {
  width: 14px;
  height: 14px;
}

.page-body {
  padding: 28px 32px;
  flex: 1;
}

/* Dialer 뷰는 패딩을 줄여 컴포넌트 자체 레이아웃을 존중합니다 */
.page-body--dialer {
  padding: 24px 32px;
}

/* Recordings 뷰는 Dialer와 동일하게 패딩을 조정하여 컴포넌트 자체 레이아웃을 존중합니다 */
.page-body--recordings {
  padding: 24px 32px;
}

/* ==================== 요약 스트립 ==================== */

.summary-strip {
  display: flex;
  gap: 16px;
  margin-bottom: 28px;
}

.summary-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 10px 16px;
  font-size: 13px;
  color: var(--color-text-secondary);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.summary-chip .chip-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.chip-dot.success { background: var(--color-success); }
.chip-dot.error   { background: var(--color-error); }
.chip-dot.warning { background: var(--color-warning); }

.summary-chip .chip-label {
  font-weight: 500;
  color: var(--color-text-primary);
}

.last-updated-bar {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-secondary);
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 10px 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.last-updated-bar svg {
  width: 13px;
  height: 13px;
  opacity: 0.6;
}

.last-updated-bar .time-value {
  font-weight: 600;
  color: var(--color-text-primary);
  font-variant-numeric: tabular-nums;
}

/* ==================== 섹션 헤더 ==================== */

.section-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.2px;
}

.section-count {
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* ==================== 상태 카드 ==================== */

.cards-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.status-card {
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.2s, transform 0.2s;
}

.status-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.card-header {
  padding: 18px 20px 14px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  border-bottom: 1px solid var(--color-border);
}

.card-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-icon-wrap.blue {
  background: #eff4ff;
}

.card-icon-wrap.purple {
  background: #f3f0ff;
}

.card-icon-wrap svg {
  width: 20px;
  height: 20px;
}

.card-icon-wrap.blue svg { color: #4a7cf7; }
.card-icon-wrap.purple svg { color: #7c3aed; }

.card-title-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.card-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.card-subtitle {
  font-size: 11.5px;
  color: var(--color-text-secondary);
}

/* 상태 배지: getStatusClass / getConnectionClass 메서드가 반환하는 클래스와 매핑 */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.status-badge .badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.badge-success {
  background: var(--color-success-bg);
  color: var(--color-success);
  border: 1px solid var(--color-success-border);
}

.badge-success .badge-dot { background: var(--color-success); }

.badge-error {
  background: var(--color-error-bg);
  color: var(--color-error);
  border: 1px solid var(--color-error-border);
}

.badge-error .badge-dot { background: var(--color-error); }

.badge-warning {
  background: var(--color-warning-bg);
  color: #b45309;
  border: 1px solid var(--color-warning-border);
}

.badge-warning .badge-dot { background: var(--color-warning); }

.badge-offline {
  background: #f3f4f6;
  color: #6b7280;
  border: 1px solid #d1d5db;
}

.badge-offline .badge-dot { background: #9ca3af; }

.card-body {
  padding: 16px 20px 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.card-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-row-label {
  font-size: 12px;
  color: var(--color-text-secondary);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.card-row-label svg {
  width: 13px;
  height: 13px;
  opacity: 0.6;
}

.card-row-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-primary);
  font-variant-numeric: tabular-nums;
}

.card-row-value.mono {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 12.5px;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.card-divider {
  height: 1px;
  background: var(--color-border);
  margin: 2px 0;
}

/* 라이브 상태 점 pulse 애니메이션 */
@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.85); }
}

.badge-dot.pulse {
  animation: pulse 2s ease-in-out infinite;
}

/* ==================== 빠른 이동 카드 ==================== */

.quick-action-section {
  margin-top: 4px;
}

.action-cards-row {
  display: flex;
  gap: 16px;
}

.action-card {
  flex: 1;
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  text-decoration: none;
  color: inherit;
  transition: box-shadow 0.2s, transform 0.2s, border-color 0.2s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  text-align: left;
  font-family: inherit;
}

.action-card:hover:not(.action-card--disabled) {
  box-shadow: 0 4px 16px rgba(74, 124, 247, 0.14);
  transform: translateY(-1px);
  border-color: #c3d5fd;
}

/* 미구현 기능 카드: 클릭 불가 */
.action-card--disabled {
  opacity: 0.6;
  cursor: default;
}

.action-card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #eff4ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-card-icon svg {
  width: 24px;
  height: 24px;
  color: var(--color-primary);
}

.action-card-icon.green-bg {
  background: var(--color-success-bg);
}

.action-card-icon.green-bg svg {
  color: var(--color-success);
}

.action-card-text {
  flex: 1;
}

.action-card-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 3px;
}

.action-card-desc {
  font-size: 12px;
  color: var(--color-text-secondary);
  line-height: 1.4;
}

.action-card-arrow {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--color-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.15s;
}

.action-card:hover:not(.action-card--disabled) .action-card-arrow {
  background: #dce8fd;
}

.action-card-arrow svg {
  width: 14px;
  height: 14px;
  color: var(--color-text-secondary);
}

/* ==================== 반응형 ==================== */

@media (max-width: 900px) {
  .cards-grid {
    grid-template-columns: 1fr;
  }

  .action-cards-row {
    flex-direction: column;
  }
}

@media (max-width: 720px) {
  .sidebar {
    width: 60px;
  }

  .logo-text,
  .nav-item span:not(.nav-icon),
  .nav-section-label,
  .sidebar-version {
    display: none;
  }

  .main-content {
    margin-left: 60px;
  }

  .logo-icon {
    margin: 0 auto;
  }

  .nav-item {
    justify-content: center;
    padding: 10px;
  }

  .page-body {
    padding: 20px 16px;
  }

  .topbar {
    padding: 0 16px;
  }

  .summary-strip {
    flex-wrap: wrap;
  }
}
</style>
