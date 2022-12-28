package edu.dkv.internal.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.lang.NonNull;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import static edu.dkv.internal.common.Constants.DEFAULT_MAX_THREADS;

@ConfigurationPropertiesScan
public class ProcessConfig {

    @NonNull
    private int count;

    @Max(10)
    private Integer maxThreads;

    // Port allocated for each process.
    // Port Range [3000, 4000]
    @NonNull @Min(3000) @Max(4000)
    private int portRangeStart;

    @NonNull @Max(10)
    private int msgReceiveTimeoutInSecs;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPortRangeStart() {
        return portRangeStart;
    }

    public void setPortRangeStart(int portRangeStart) {
        this.portRangeStart = portRangeStart;
    }

    public int getMsgReceiveTimeoutInSecs() {
        return msgReceiveTimeoutInSecs;
    }

    public void setMsgReceiveTimeoutInSecs(int msgReceiveTimeoutInSecs) {
        this.msgReceiveTimeoutInSecs = msgReceiveTimeoutInSecs;
    }

    public int getMaxThreads() {
        return maxThreads != null ? maxThreads : DEFAULT_MAX_THREADS;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    @Override
    public String toString() {
        return "ProcessConfig{" +
                "\n count=" + count +
                ",\n maxThreads=" + maxThreads +
                ",\n portRangeStart=" + portRangeStart +
                ",\n msgReceiveTimeoutInSecs=" + msgReceiveTimeoutInSecs +
                "\n}";
    }
}
