<template>
  <div id="main-app">
    <div v-if="currentView === 'status'" class="container">
      <h1>Simple CTI</h1>

      <div class="status-panel">
        <h2>System Status</h2>
        <p v-if="lastUpdated" class="last-checked">
          Last Update {{ formatTime(lastUpdated) }}
        </p>
        <div class="divider"></div>
        
        <div class="status-group">
          <h3>Application Server</h3>
          <p>
            <strong>Status:</strong> 
            <span :class="getStatusClass(appStatus.status)">
              {{ appStatus.status }}
            </span>
          </p>
        </div>

        <div class="divider"></div>

        <div class="status-group">
          <h3>Asterisk Connection</h3>
          <p><strong>Host:</strong> <span>{{ asteriskStatus.host }}</span></p>
          <p><strong>User:</strong> <span>{{ asteriskStatus.username }}</span></p>
          <p>
            <strong>State:</strong> 
            <span :class="getConnectionClass(asteriskStatus.connectionState)">
              {{ asteriskStatus.connectionState }}
            </span>
          </p>
          <p><strong>Version:</strong> <span>{{ asteriskStatus.asteriskVersion }}</span></p>
        </div>
      </div>

      <button @click="currentView = 'dial'" class="button">Call</button>
    </div>

    <Dialer v-if="currentView === 'dial'" @back="currentView = 'status'" />
  </div>
</template>

<script>
import Dialer from './components/Dialer.vue'

export default {
  name: 'App',
  components: {
    Dialer
  },
  data() {
    return {
      currentView: 'status', // 'status' or 'dial'
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
    // Schedule health check every 30 seconds
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
    getStatusClass(status) {
      if (status === 'UP') return 'status-ok';
      if (status === 'CHECKING...') return 'status-pending';
      return 'status-error';
    },
    getConnectionClass(state) {
      if (state === 'CONNECTED') return 'status-ok';
      if (state === 'UNKNOWN') return 'status-pending';
      return 'status-error';
    },
    formatTime(timestamp) {
      if (!timestamp) return '';
      return new Date(timestamp).toLocaleTimeString('en-GB', { hour12: false });
    }
  }
}
</script>

<style>
body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    background-color: #f0f2f5;
    color: #333;
    margin: 0;
    padding: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
}
#main-app {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
}
.container {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    background-color: #ffffff;
    padding: 40px;
    border-radius: 8px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    margin: 20px;
    max-width: 90%;
    min-width: 320px;
}
h1 {
    color: #1c1e21;
    font-size: 28px;
    margin-bottom: 16px;
}
.button {
    display: inline-block;
    margin-top: 20px;
    padding: 10px 20px;
    background-color: #007bff;
    color: white;
    text-decoration: none;
    border-radius: 5px;
    border: none;
    font-size: 16px;
    font-weight: bold;
    cursor: pointer;
    transition: background-color 0.3s ease;
}
.button:hover {
    background-color: #0056b3;
}
.status-panel {
    margin-top: 20px;
    padding: 20px;
    background-color: #f8f9fa;
    border-radius: 8px;
    border: 1px solid #e9ecef;
    text-align: left;
    width: 100%;
    box-sizing: border-box;
}
.status-panel h2 {
    font-size: 20px;
    margin-top: 0;
    margin-bottom: 5px;
    color: #495057;
    text-align: center;
}
.status-group {
    margin-bottom: 10px;
}
.status-group h3 {
    font-size: 16px;
    margin: 0 0 10px 0;
    color: #6c757d;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}
.status-panel p {
    margin: 8px 0;
    font-size: 14px;
    color: #495057;
}
.status-panel .status-group p {
    display: flex;
    justify-content: space-between;
}
.status-panel strong {
    color: #343a40;
    font-weight: 600;
}
.divider {
    height: 1px;
    background-color: #e9ecef;
    margin: 15px 0;
}
.status-ok {
    color: #28a745;
    font-weight: bold;
}
.status-error {
    color: #dc3545;
    font-weight: bold;
}
.status-pending {
    color: #ffc107;
    font-weight: bold;
}
.timestamp {
    font-size: 12px !important;
    color: #adb5bd !important;
    text-align: right;
}
.last-checked {
    font-size: 12px;
    color: #6c757d;
    text-align: center;
    margin-top: 0;
    margin-bottom: 10px;
    font-style: italic;
    display: block;
    width: 100%;
}
</style>