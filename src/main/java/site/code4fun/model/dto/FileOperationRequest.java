package site.code4fun.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FileOperationRequest {
    private RequestAction action;
    private String path;
    private boolean showHiddenItems;
    private List<FileManagerDirectoryContent> data;
    private String name;
    private String newName;
    private String targetPath;
    private List<String> names;
    private String searchString;

    public enum RequestAction {
        details,read,delete,copy,move,create,search,rename
    }
}