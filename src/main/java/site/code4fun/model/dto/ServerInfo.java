package site.code4fun.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ServerInfo implements Serializable {

    @JsonProperty("upload_max_filesize")
    private int uploadMaxFilesize;

    @JsonProperty("memory_limit")
    private String memoryLimit;

    @JsonProperty("max_execution_time")
    private String maxExecutionTime;

    @JsonProperty("max_input_time")
    private String maxInputTime;

    @JsonProperty("post_max_size")
    private int postMaxSize;
}