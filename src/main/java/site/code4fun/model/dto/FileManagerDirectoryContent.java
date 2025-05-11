package site.code4fun.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileManagerDirectoryContent {

    private Long id;
    private String name;  // File name
    private String dateCreated;  // Date when the file was created (UTC)
    private String dateModified;  // Date when the file was last modified (UTC)
    private String filterPath;  // Relative path to the file or folder
    private boolean hasChild;  // Defines if the folder has any child folders
    @JsonProperty("isFile")
    private boolean isFile;  // Whether the item is a file or a folder
    private long size;  // File size
    private String type;  // File extension (e.g., .txt, .jpg)
    private Boolean caseSensitive;  // If search is case sensitive or not
    private String action;  // Name of the file operation (e.g., read)
    private List<String> names;  // List of item names to be downloaded
    private FileManagerDirectoryContent data;  // Details of the download item
//    private List<IFormFile> uploadFiles;  // Uploaded files
    private String newName;  // New name for the item
    private String link;  // New name for the item
    private String searchString;  // String to search for in the directory
    private String targetPath;  // Relative path where the items to be pasted
    private FileManagerDirectoryContent targetData;  // Details of the copied item
    private List<String> renameFiles;
}