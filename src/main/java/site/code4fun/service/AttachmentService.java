package site.code4fun.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.code4fun.constant.SearchOperator;
import site.code4fun.model.AppSettingEntity;
import site.code4fun.model.Attachment;
import site.code4fun.model.User;
import site.code4fun.model.dto.FileManagerDirectoryContent;
import site.code4fun.model.dto.FileOperationRequest;
import site.code4fun.model.dto.FileOperationResponse;
import site.code4fun.model.dto.SearchCriteria;
import site.code4fun.model.mapper.AttachmentMapper;
import site.code4fun.repository.jpa.AppSettingRepository;
import site.code4fun.repository.jpa.AttachmentRepository;
import site.code4fun.repository.jpa.UserRepository;
import site.code4fun.service.storage.CloudStorage;
import site.code4fun.service.storage.CloudStorageFactory;
import site.code4fun.service.storage.GoogleCloudStore;
import site.code4fun.util.SecurityUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.*;
import static site.code4fun.constant.AppConstants.STORAGE_PROVIDER_KEY;
import static site.code4fun.util.FileUtils.downLoadToLocal;
import static site.code4fun.util.FileUtils.urlToMultipartFile;

/**
 * Service xử lý các thao tác với file đính kèm
 */
@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class AttachmentService extends AbstractBaseService<Attachment, Long> {
    private final CloudStorageFactory storageFactory;
    private final UserRepository userRepository;
    private final AttachmentRepository repository;
    private final AppSettingRepository appSettingRepository;
    private final AttachmentMapper attachmentMapper;

    @Override
    @Transactional
    public void delete(Long id) {
        Optional.ofNullable(getById(id))
                .ifPresent(this::deleteAttachment);
    }

    @Override
    protected Specification<Attachment> createSpecification(List<SearchCriteria> criteriaList, String queryString) {
        criteriaList.removeIf(criteria -> "size".equalsIgnoreCase(criteria.getKey()));

        if (!SecurityUtils.isAdmin()) {
            criteriaList.add(new SearchCriteria("users", SearchOperator.IN, SecurityUtils.getUserId()));
        }
        return super.createSpecification(criteriaList, queryString);
    }

    @Transactional
    public Attachment update(Attachment entity) {
        return Optional.ofNullable(getById(entity.getId()))
                .map(attachment -> updateAttachmentFields(attachment, entity))
                .map(repository::save)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy attachment với id: " + entity.getId()));
    }

    public List<Attachment> upload(MultipartFile[] files) {
        return upload(files, null);
    }

    public List<Attachment> upload(MultipartFile[] files, String provider) {
        CloudStorage storage = getStorageProvider(provider);
        return Arrays.stream(files)
                .map(file -> uploadFile(file, storage))
                .collect(Collectors.toList());
    }

    @Transactional
    public Attachment upload(MultipartFile file) {
        return upload(new MultipartFile[] { file }).get(0);
    }

    public List<String> getAllContentType() {
        return getRepository().findAllContentType();
    }

    public List<String> getAllStorageProvider() {
        return storageFactory.getProviders();
    }

    public List<Attachment> sync(String provider) {
        String targetProvider = isNotEmpty(provider) ? provider : GoogleCloudStore.class.getSimpleName();
        return Optional.ofNullable(SecurityUtils.getUserId())
                .flatMap(userId -> userRepository.findById(userId))
                .filter(this::isExistToken)
                .map(user -> storageFactory.getProvider(targetProvider).getAll())
                .map(getRepository()::saveAll)
                .orElse(Collections.emptyList());
    }

    @Transactional
    public FileOperationResponse getFiles(FileOperationRequest request) {
        FileManagerDirectoryContent cwd = createDefaultDirectory();
        List<FileManagerDirectoryContent> items = new ArrayList<>();

        processFileOperation(request, cwd);
        items.addAll(getAll().stream()
                .map(attachmentMapper::entityToFileManagerContent)
                .collect(Collectors.toList()));

        return buildFileOperationResponse(cwd, items);
    }

    public String getImage(String path) {
        return getRepository().findByName(path.replaceAll("/", ""))
                .stream()
                .findFirst()
                .map(Attachment::getLink)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ảnh với đường dẫn: " + path));
    }

    public void downloadImage(String imageUrl, String fileName) {
        try {
            downLoadToLocal(imageUrl, fileName);
        } catch (Exception e) {
            log.error("Lỗi khi tải ảnh {}: {}", imageUrl, e.getMessage());
        }
    }

    public Attachment createAttachment(String url) {
        String fileName = generateFileName();
        MultipartFile file = urlToMultipartFile(url, fileName, "image/jpeg");
        return upload(file);
    }

    // Private methods
    private void deleteAttachment(Attachment attachment) {
        attachment.getProducts().forEach(p -> p.removeFile(attachment));
        deleteFromStorage(attachment);
        getRepository().delete(attachment);
    }

    private void deleteFromStorage(Attachment attachment) {
        try {
            log.info("Bắt đầu xóa file '{}' từ {}", attachment.getName(), attachment.getStorageProvider());
            storageFactory.getProvider(attachment.getStorageProvider()).delete(attachment);
            log.info("Đã xóa file '{}' từ {}", attachment.getName(), attachment.getStorageProvider());
        } catch (Exception e) {
            log.error("Lỗi khi xóa file '{}': {}", attachment.getName(), e.getMessage());
        }
    }

    private Attachment updateAttachmentFields(Attachment attachment, Attachment entity) {
        attachment.setName(entity.getName());
        attachment.setAuthor(entity.getAuthor());
        attachment.setContent(entity.getContent());
        attachment.setThumbnail(entity.getThumbnail());
        attachment.setStatus(entity.getStatus());
        return attachment;
    }

    private Attachment uploadFile(MultipartFile file, CloudStorage storage) {
        log.info("Bắt đầu upload file '{}' lên {}", file.getName(), storage.getStorageProvider());
        Attachment attachment = storage.upload(file, String.valueOf(System.currentTimeMillis()), file.getContentType());
        return getRepository().save(attachment);
    }

    private boolean isExistToken(User user) {
        return isNotBlank(user.getToken()) || isNotBlank(user.getRefreshToken());
    }

    private CloudStorage getStorageProvider(String provider) {
        String providerName = Optional.ofNullable(provider)
                .filter(p -> isNotBlank(p))
                .orElseGet(this::getDefaultProvider);
        return storageFactory.getProvider(providerName);
    }

    private String getDefaultProvider() {
        return appSettingRepository.findFirstByKey(STORAGE_PROVIDER_KEY)
                .map(AppSettingEntity::getValue)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy cấu hình storage provider"));
    }

    private FileManagerDirectoryContent createDefaultDirectory() {
        return FileManagerDirectoryContent.builder()
                .size(0)
                .isFile(false)
                .name("Files")
                .hasChild(false)
                .build();
    }

    private void processFileOperation(FileOperationRequest request, FileManagerDirectoryContent cwd) {
        switch (request.getAction()) {
            case read -> log.info("Đang xử lý đọc files từ '{}'", request.getAction());
            case delete -> request.getData().forEach(blob -> this.delete(blob.getId()));
            case details -> handleDetailsRequest(request, cwd);
            case create -> createFolder(request.getName());
            case copy -> throw new UnsupportedOperationException("Unimplemented case: " + request.getAction());
            case move -> throw new UnsupportedOperationException("Unimplemented case: " + request.getAction());
            case rename -> throw new UnsupportedOperationException("Unimplemented case: " + request.getAction());
            case search -> throw new UnsupportedOperationException("Unimplemented case: " + request.getAction());
            default -> throw new IllegalArgumentException("Unexpected value: " + request.getAction());
        }
    }

    private void handleDetailsRequest(FileOperationRequest request, FileManagerDirectoryContent cwd) {
        if (!request.getData().isEmpty()) {
            Optional.ofNullable(request.getData().get(0).getId())
                    .map(this::getById)
                    .map(attachmentMapper::entityToFileManagerContent)
                    .ifPresent(details -> {
                        cwd.setSize(details.getSize());
                        cwd.setName(details.getName());
                    });
        }
    }

    private void createFolder(String name) {
        String folderName = getStorageProvider(null).createFolder(name);
        log.info("Đã tạo thư mục với tên '{}'", folderName);
    }

    private FileOperationResponse buildFileOperationResponse(FileManagerDirectoryContent cwd,
            List<FileManagerDirectoryContent> items) {
        return FileOperationResponse.builder()
                .cwd(cwd)
                .details(cwd)
                .files(items)
                .build();
    }

    private String generateFileName() {
        return System.currentTimeMillis() + ".jpeg";
    }
}
