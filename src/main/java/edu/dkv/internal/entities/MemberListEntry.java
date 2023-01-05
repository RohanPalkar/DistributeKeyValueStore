package edu.dkv.internal.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.InetAddress;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class MemberListEntry {

    private EndPoint endPoint;
    private long heartbeat;
    private long timestamp;

    public MemberListEntry(EndPoint endPoint) {
        this.endPoint = endPoint;
    }

    public MemberListEntry(InetAddress address, int port, long heartbeat, long timestamp) {
        this.endPoint = new EndPoint(address, port);
        this.heartbeat = heartbeat;
        this.timestamp = timestamp;
    }

    public MemberListEntry(EndPoint endPoint, long heartbeat, long timestamp) {
        this.endPoint = endPoint;
        this.heartbeat = heartbeat;
        this.timestamp = timestamp;
    }

    public EndPoint getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(EndPoint endPoint) {
        this.endPoint = endPoint;
    }

    public long getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(long heartbeat) {
        this.heartbeat = heartbeat;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberListEntry that = (MemberListEntry) o;
        return endPoint.equals(that.endPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endPoint);
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

    /**
     * Creates and returns a copy of this object.  The precise meaning
     * of "copy" may depend on the class of the object.
     *
     * @return a clone of this instance.
     * @throws CloneNotSupportedException if the object's class does not
     *                                    support the {@code Cloneable} interface. Subclasses
     *                                    that override the {@code clone} method can also
     *                                    throw this exception to indicate that an instance cannot
     *                                    be cloned.
     * @see Cloneable
     */
    @Override
    protected MemberListEntry clone() throws CloneNotSupportedException {
        return new MemberListEntry(this.endPoint.getAddress(), this.endPoint.getPort(), this.heartbeat, this.timestamp);
    }
}
