package simple.simple_cti.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 통화 이력(CDR) JPA Entity.
 * Asterisk AMI CdrEvent 데이터를 영속 저장한다.
 * 테이블: cdr
 */
@Entity
@Table(name = "cdr")
public class Cdr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "accountcode", length = 20)
    private String accountcode;

    @Column(name = "src", nullable = false, length = 80)
    private String src;

    @Column(name = "dst", nullable = false, length = 80)
    private String dst;

    @Column(name = "dcontext", length = 80)
    private String dcontext;

    @Column(name = "clid", length = 80)
    private String clid;

    @Column(name = "channel", length = 80)
    private String channel;

    @Column(name = "dstchannel", length = 80)
    private String dstchannel;

    @Column(name = "lastapp", length = 80)
    private String lastapp;

    @Column(name = "lastdata", length = 80)
    private String lastdata;

    @Column(name = "calldate", nullable = false)
    private LocalDateTime calldate;

    @Column(name = "answer")
    private LocalDateTime answer;

    @Column(name = "end")
    private LocalDateTime end;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "billsec")
    private Integer billsec;

    @Column(name = "disposition", length = 45)
    private String disposition;

    @Column(name = "amaflags", length = 45)
    private String amaflags;

    // Asterisk 고유 통화 ID — UNIQUE 제약으로 중복 저장 방지
    @Column(name = "uniqueid", nullable = false, unique = true, length = 32)
    private String uniqueid;

    @Column(name = "userfield", length = 255)
    private String userfield;

    @Column(name = "recording_file", length = 255)
    private String recordingFile;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Cdr() {
    }

    public Long getId() {
        return id;
    }

    public String getAccountcode() {
        return accountcode;
    }

    public void setAccountcode(String accountcode) {
        this.accountcode = accountcode;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }

    public String getDcontext() {
        return dcontext;
    }

    public void setDcontext(String dcontext) {
        this.dcontext = dcontext;
    }

    public String getClid() {
        return clid;
    }

    public void setClid(String clid) {
        this.clid = clid;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getDstchannel() {
        return dstchannel;
    }

    public void setDstchannel(String dstchannel) {
        this.dstchannel = dstchannel;
    }

    public String getLastapp() {
        return lastapp;
    }

    public void setLastapp(String lastapp) {
        this.lastapp = lastapp;
    }

    public String getLastdata() {
        return lastdata;
    }

    public void setLastdata(String lastdata) {
        this.lastdata = lastdata;
    }

    public LocalDateTime getCalldate() {
        return calldate;
    }

    public void setCalldate(LocalDateTime calldate) {
        this.calldate = calldate;
    }

    public LocalDateTime getAnswer() {
        return answer;
    }

    public void setAnswer(LocalDateTime answer) {
        this.answer = answer;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getBillsec() {
        return billsec;
    }

    public void setBillsec(Integer billsec) {
        this.billsec = billsec;
    }

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public String getAmaflags() {
        return amaflags;
    }

    public void setAmaflags(String amaflags) {
        this.amaflags = amaflags;
    }

    public String getUniqueid() {
        return uniqueid;
    }

    public void setUniqueid(String uniqueid) {
        this.uniqueid = uniqueid;
    }

    public String getUserfield() {
        return userfield;
    }

    public void setUserfield(String userfield) {
        this.userfield = userfield;
    }

    public String getRecordingFile() {
        return recordingFile;
    }

    public void setRecordingFile(String recordingFile) {
        this.recordingFile = recordingFile;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
