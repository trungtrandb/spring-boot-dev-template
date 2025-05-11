package site.code4fun.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class SignalRequest implements Serializable {
    private String type;
    private String candidate;
    private String sdpMid;
    private String sdpMLineIndex;
    private String sessionId;
    private String sdp;
    private Long roomId;
}
