<template>
  <div id="main-app">
    <div v-if="currentView === 'status'" class="container">
      <h1>Simple CTI</h1>

      <div class="status-panel">
        <h2>Connection Status</h2>
        <p><strong>Host:</strong> <span>{{ status.host }}</span></p>
        <p><strong>User:</strong> <span>{{ status.username }}</span></p>
        <p><strong>State:</strong> <span>{{ status.connectionState }}</span></p>
        <p><strong>Version:</strong> <span>{{ status.asteriskVersion }}</span></p>
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
      status: {
        host: 'Loading...',
        username: 'Loading...',
        connectionState: 'UNKNOWN',
        asteriskVersion: 'Loading...'
      }
    }
  },
  mounted() {
    this.fetchStatus();
  },
  methods: {
    async fetchStatus() {
      try {
        const response = await fetch('/api/status');
        if (response.ok) {
          const data = await response.json();
          this.status = data;
        } else {
          console.error('Failed to fetch status');
          this.status.connectionState = 'ERROR';
        }
      } catch (error) {
        console.error('Error fetching status:', error);
        this.status.connectionState = 'OFFLINE';
      }
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
    margin-top: 30px;
    padding: 20px;
    background-color: #f8f9fa;
    border-radius: 8px;
    border: 1px solid #e9ecef;
    text-align: left;
    min-width: 250px;
}
.status-panel h2 {
    font-size: 20px;
    margin-top: 0;
    margin-bottom: 15px;
    color: #495057;
    text-align: center;
}
.status-panel p {
    margin: 8px 0;
    font-size: 14px;
    color: #6c757d;
}
.status-panel strong {
    color: #343a40;
    font-weight: 600;
}
</style>