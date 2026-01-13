<template>
  <div class="dialer-container">
    <div class="main-layout">
      <!-- Left Panel: Dialer -->
      <div class="dialer-panel">
        <div class="status-indicator" :class="callStatus.toLowerCase()">
          {{ callStatus }}
        </div>

        <div class="input-wrapper" style="margin-top: 10px;">
          <input type="text" v-model="targetNumber" placeholder="Phone Number" @keyup.enter="dial" :disabled="callStatus === 'InCall' || callStatus === 'Ringing'">
          <button v-if="targetNumber && callStatus !== 'InCall' && callStatus !== 'Ringing'" @click="backspace" class="backspace-btn">⌫</button>
        </div>

        <div class="keypad">
          <button v-for="key in keys" :key="key" @click="appendDigit(key)" class="keypad-btn" :disabled="callStatus === 'InCall' || callStatus === 'Ringing'">
            {{ key }}
          </button>
        </div>

        <div class="dial-options">
          <label>
            <input type="checkbox" v-model="recordingEnabled" :disabled="callStatus !== 'Registered'"> Record Call
          </label>
        </div>
        
        <div class="action-buttons">
          <button v-if="callStatus === 'Registered' || callStatus === 'Idle'" @click="dial" class="dial-btn">📞</button>
          <button v-else @click="hangup" class="hangup-btn">🤙</button>
        </div>
        
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

    <!-- Hidden audio element for WebRTC -->
    <audio ref="remoteAudio" autoplay></audio>
  </div>
</template>

<script>
import { UserAgent, Registerer, Inviter, Web } from 'sip.js';

export default {
  name: 'DialerPage',
  data() {
    return {
      targetNumber: '',
      recordingEnabled: false,
      recordings: [],
      selectedRecordings: [],
      keys: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '*', '0', '#'],
      
      // SIP / WebRTC state
      userAgent: null,
      registerer: null,
      session: null,
      callStatus: 'Idle', // Idle, Registering, Registered, Ringing, InCall
      sipConfig: {
        server: process.env.VUE_APP_SIP_SERVER,
        uri: process.env.VUE_APP_SIP_URI,
        password: process.env.VUE_APP_SIP_PASSWORD,
        displayName: process.env.VUE_APP_SIP_DISPLAY_NAME
      }
    }
  },
  methods: {
    async initSip() {
      const transportOptions = {
        server: this.sipConfig.server
      };

      const uri = UserAgent.makeURI(this.sipConfig.uri);
      if (!uri) {
        console.error("Invalid URI");
        return;
      }

      this.userAgent = new UserAgent({
        uri: uri,
        transportOptions: transportOptions,
        authorizationUsername: uri.user,
        authorizationPassword: this.sipConfig.password,
        displayName: this.sipConfig.displayName,
        delegate: {
          onInvite: (invitation) => {
            console.log("Incoming call...");
            this.handleIncomingCall(invitation);
          }
        }
      });

      try {
        await this.userAgent.start();
        console.log("UserAgent started");
        this.callStatus = 'Registering';

        this.registerer = new Registerer(this.userAgent);
        await this.registerer.register();
        this.callStatus = 'Registered';
        console.log("Registered to SIP server");
      } catch (error) {
        console.error("Failed to start UserAgent or Register", error);
        this.callStatus = 'Idle';
      }
    },
    handleIncomingCall(invitation) {
      this.session = invitation;
      this.callStatus = 'Ringing';
      // For now, auto-accept or add UI for answer
      if (confirm(`Incoming call from ${invitation.remoteIdentity.uri.user}. Answer?`)) {
        this.answerCall();
      } else {
        invitation.reject();
        this.callStatus = 'Registered';
      }
    },
    async answerCall() {
      if (!this.session) return;
      
      const options = {
        sessionDescriptionHandlerOptions: {
          constraints: { audio: true, video: false }
        }
      };

      this.setupSessionDelegate(this.session);
      await this.session.accept(options);
      this.callStatus = 'InCall';
    },
    setupSessionDelegate(session) {
      session.delegate = {
        onCallTerminated: () => {
          console.log("Call terminated");
          this.session = null;
          this.callStatus = 'Registered';
        },
        onSessionDescriptionHandler: (sdh) => {
          const remoteStream = new MediaStream();
          sdh.peerConnection.getReceivers().forEach((receiver) => {
            if (receiver.track) {
              remoteStream.addTrack(receiver.track);
            }
          });
          this.$refs.remoteAudio.srcObject = remoteStream;
          this.$refs.remoteAudio.play();
        }
      };
    },
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

      if (!this.userAgent || this.callStatus === 'Idle') {
        alert("SIP is not registered. Please check configuration.");
        return;
      }

      const targetURI = UserAgent.makeURI(`sip:${this.targetNumber}@${this.sipConfig.uri.split('@')[1]}`);
      if (!targetURI) {
        alert("Invalid target number.");
        return;
      }

      this.session = new Inviter(this.userAgent, targetURI);
      this.setupSessionDelegate(this.session);

      const options = {
        sessionDescriptionHandlerOptions: {
          constraints: { audio: true, video: false }
        }
      };

      try {
        await this.session.invite(options);
        this.callStatus = 'Ringing';
        console.log("Dialing...");
      } catch (error) {
        console.error("Failed to place call", error);
        alert("Failed to place call.");
      }
    },
    async hangup() {
      if (!this.session) return;

      if (this.session instanceof Inviter && this.callStatus === 'Ringing') {
        await this.session.cancel();
      } else {
        await this.session.bye();
      }
      
      this.session = null;
      this.callStatus = 'Registered';
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
    this.initSip();
    // this.loadRecordings();
  },
  beforeUnmount() {
    if (this.registerer) {
      this.registerer.unregister();
    }
    if (this.userAgent) {
      this.userAgent.stop();
    }
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
.status-indicator {
    padding: 5px 10px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: bold;
    display: inline-block;
    margin-bottom: 10px;
    background-color: #eee;
    color: #666;
}
.status-indicator.registered {
    background-color: #d4edda;
    color: #155724;
}
.status-indicator.incall, .status-indicator.ringing {
    background-color: #fff3cd;
    color: #856404;
    animation: blink 1s infinite;
}
@keyframes blink {
    50% { opacity: 0.5; }
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
.hangup-btn {
    width: 60px;
    height: 60px;
    padding: 0;
    font-size: 24px;
    background-color: #dc3545;
    color: white;
    border: none;
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    justify-content: center;
    align-items: center;
    margin: 0 auto;
    transform: rotate(135deg);
}
.hangup-btn:hover {
    background-color: #c82333;
}
.action-buttons {
    display: flex;
    justify-content: center;
    margin-top: 10px;
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
