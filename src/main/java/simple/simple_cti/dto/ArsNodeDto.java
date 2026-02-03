package simple.simple_cti.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArsNodeDto {
    private String id;
    private String type;
    private String audio;
    private String next;
    private Integer timeout;
    private Integer maxAttempts;
    private Map<String, String> routes;
    private String noInput;
    private String invalid;
    private String destination;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAudio() { return audio; }
    public void setAudio(String audio) { this.audio = audio; }

    public String getNext() { return next; }
    public void setNext(String next) { this.next = next; }

    public Integer getTimeout() { return timeout; }
    public void setTimeout(Integer timeout) { this.timeout = timeout; }

    public Integer getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }

    public Map<String, String> getRoutes() { return routes; }
    public void setRoutes(Map<String, String> routes) { this.routes = routes; }

    public String getNoInput() { return noInput; }
    public void setNoInput(String noInput) { this.noInput = noInput; }

    public String getInvalid() { return invalid; }
    public void setInvalid(String invalid) { this.invalid = invalid; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
}