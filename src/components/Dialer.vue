<template>
  <div class="dialer-container">
    <h2>Dial</h2>
    <input type="text" v-model="targetNumber" placeholder="Phone Number" @keyup.enter="dial">
    <br>
    <label>
      <input type="checkbox" v-model="recordingEnabled"> Record Call
    </label>
    <br><br>
    <button @click="dial">Dial</button>

    <hr style="width: 100%; margin: 20px 0;">
    
    <h3>Recordings</h3>
    <button @click="loadRecordings" style="background-color: #28a745; margin-bottom: 10px;">Refresh List</button>
    <div id="recordingList" class="recording-list">
      <p v-if="recordings.length === 0">No recordings found.</p>
      <div v-for="file in recordings" :key="file" class="recording-item">
        <input type="checkbox" :value="file" v-model="selectedRecordings" class="rec-checkbox">
        <span>{{ file }}</span>
      </div>
    </div>
    <br>
    <button @click="downloadSelected" style="background-color: #17a2b8;">Download Selected</button>
    
    <br>
    <button @click="$emit('back')" style="background-color: #6c757d; margin-top: 20px;">Back to Status</button>
  </div>
</template>

<script>
export default {
  name: 'DialerPage',
  data() {
    return {
      targetNumber: '',
      recordingEnabled: false,
      recordings: [],
      selectedRecordings: []
    }
  },
  methods: {
    async dial() {
      if (!this.targetNumber) {
        alert("Please enter a phone number.");
        return;
      }

      try {
        const response = await fetch(`/originate?targetNumber=${encodeURIComponent(this.targetNumber)}&recordingEnabled=${this.recordingEnabled}`, {
          method: 'POST'
        });
        const data = await response.json();
        if(data.success) {
          alert('Dialing ' + this.targetNumber + '...');
        } else {
          alert('Failed to initiate call.');
        }
      } catch (error) {
        console.error('Error:', error);
        alert('An error occurred while dialing.');
      }
    },
    async loadRecordings() {
      try {
        const response = await fetch('/api/recordings/list');
        const files = await response.json();
        this.recordings = files.sort().reverse();
      } catch (error) {
        console.error('Error loading recordings:', error);
      }
    },
    downloadSelected() {
      if (this.selectedRecordings.length === 0) {
        alert('Please select at least one recording to download.');
        return;
      }

      this.selectedRecordings.forEach(filename => {
        const link = document.createElement('a');
        link.href = '/api/recordings/' + encodeURIComponent(filename);
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      });
    }
  },
  mounted() {
    // this.loadRecordings();
  }
}
</script>

<style scoped>
.dialer-container {
    text-align: center;
    padding: 20px;
    border: 1px solid #ccc;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    background-color: white;
}
input[type="text"] {
    padding: 10px;
    font-size: 16px;
    margin-bottom: 10px;
    border-radius: 4px;
    border: 1px solid #ddd;
    width: 200px;
}
button {
    padding: 10px 20px;
    font-size: 16px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.3s;
}
button:hover {
    background-color: #0056b3;
}
.recording-list {
    text-align: left;
    max-height: 200px;
    overflow-y: auto;
    width: 100%;
    border: 1px solid #eee;
    padding: 10px;
    margin-top: 10px;
}
.recording-item {
    padding: 5px;
    border-bottom: 1px solid #eee;
    display: flex;
    align-items: center;
}
.rec-checkbox {
    margin-right: 10px;
}
</style>
