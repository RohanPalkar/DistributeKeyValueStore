package edu.dkv.internal.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class UserProcess {

    private static final String PROCESS_NAME_PREFIX = "process_";
    private final String processName;
    private final int processId;
    private final EndPoint endPoint;

    public UserProcess(int index, int port) throws UnknownHostException {
        this.processName = PROCESS_NAME_PREFIX.concat(String.valueOf(index));
        this.processId = index;
        this.endPoint = new EndPoint(InetAddress.getLocalHost(), port);
    }

    public String getProcessName() {
        return processName;
    }

    public EndPoint getEndPoint() {
        return endPoint;
    }

    public int getProcessId() {
        return processId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProcess that = (UserProcess) o;
        return processId == that.processId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(processId);
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
