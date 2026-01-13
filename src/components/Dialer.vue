<template>
  <div class="dialer-container">
    <div class="main-layout">
      <!-- Left Panel: Dialer -->
      <div class="dialer-panel">
        <div class="input-wrapper" style="margin-top: 20px;">
          <input type="text" v-model="targetNumber" placeholder="Phone Number" @keyup.enter="dial">
          <button v-if="targetNumber" @click="backspace" class="backspace-btn">⌫</button>
        </div>

        <div class="keypad">
          <button v-for="key in keys" :key="key" @click="appendDigit(key)" class="keypad-btn">
            {{ key }}
          </button>
        </div>

        <div class="dial-options">
          <label>
            <input type="checkbox" v-model="recordingEnabled"> Record Call
          </label>
        </div>
        
        <button @click="dial" class="dial-btn">📞</button>
        
        <button @click="$emit('back')" style="background-color: #6c757d; margin-top: 40px; width: 100%; padding: 10px; color: white; border: none; border-radius: 4px; cursor: pointer;">Back to Status</button>
      </div>

      <!-- Right Panel: Recordings -->
      <div class="recordings-panel">
        <h3>Recordings</h3>
        <button @click="loadRecordings" style="background-color: #28a745; margin-bottom: 10px; width: 100%; padding: 8px; color: white; border: none; border-radius: 4px; cursor: pointer;">Refresh List</button>
        <div id="recordingList" class="recording-list">
          <p v-if="recordings.length === 0" style="padding: 10px; color: #666;">No recordings found.</p>
          <div v-for="file in recordings" :key="file" class="recording-item">
            <input type="checkbox" :value="file" v-model="selectedRecordings" class="rec-checkbox">
            <span :title="file" style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: block; width: 100%;">{{ file }}</span>
          </div>
        </div>
        <br>
        <button @click="downloadSelected" style="background-color: #17a2b8; width: 100%; padding: 10px; color: white; border: none; border-radius: 4px; cursor: pointer;">Download Selected</button>
      </div>
    </div>
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
      selectedRecordings: [],
      keys: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '*', '0', '#']
    }
  },
  methods: {
    appendDigit(digit) {
      this.targetNumber += digit;
    },
    backspace() {
      this.targetNumber = this.targetNumber.slice(0, -1);
    },
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
    max-width: 800px;
    margin: 0 auto;
}
.main-layout {
    display: flex;
    gap: 30px;
    align-items: flex-start;
}
.dialer-panel {
    flex: 1;
    max-width: 350px;
}
.recordings-panel {
    flex: 1;
    border-left: 1px solid #eee;
    padding-left: 30px;
    display: flex;
    flex-direction: column;
}

.input-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20px;
    position: relative;
}
input[type="text"] {
    padding: 12px;
    font-size: 20px;
    border-radius: 4px;
    border: 1px solid #ddd;
    width: 100%;
    text-align: center;
}
.backspace-btn {
    position: absolute;
    right: 10px;
    background: none;
    border: none;
    color: #666;
    font-size: 20px;
    cursor: pointer;
    padding: 5px;
}
.backspace-btn:hover {
    color: #333;
    background: none;
}
.keypad {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
    margin-bottom: 20px;
}
.keypad-btn {
    padding: 15px;
    font-size: 18px;
    background-color: #f8f9fa;
    color: #333;
    border: 1px solid #ddd;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.2s;
}
.keypad-btn:hover {
    background-color: #e9ecef;
}
.keypad-btn:active {
    background-color: #dee2e6;
}
.dial-options {
    margin-bottom: 20px;
}
.dial-btn {
    width: 60px;
    height: 60px;
    padding: 0;
    font-size: 24px;
    background-color: #28a745;
    color: white;
    border: none;
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    justify-content: center;
    align-items: center;
    margin: 0 auto;
}
.dial-btn:hover {
    background-color: #218838;
}
.recording-list {
    text-align: left;
    height: 300px;
    overflow-y: auto;
    width: 100%;
    border: 1px solid #eee;
    padding: 5px;
    margin-top: 10px;
    background-color: #fafafa;
    border-radius: 4px;
}
.recording-item {
    padding: 8px;
    border-bottom: 1px solid #eee;
    display: flex;
    align-items: center;
    font-size: 14px;
}
.recording-item:last-child {
    border-bottom: none;
}
.rec-checkbox {
    margin-right: 10px;
}
</style>
