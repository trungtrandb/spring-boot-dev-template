package site.code4fun.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.s3.model.ErrorDetails;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileOperationResponse {
    private FileManagerDirectoryContent cwd;
    private List<FileManagerDirectoryContent> files;
    private ErrorDetails error;
    private FileManagerDirectoryContent details;
}