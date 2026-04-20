package manage.store.inventory.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import manage.store.inventory.exception.BusinessException;
import manage.store.inventory.exception.ResourceNotFoundException;

/**
 * LocalStorageService — disk-based impl of StorageService.
 * Files stored under `${storage.local.root}` (default /var/hoi/uploads).
 * URLs returned are relative paths rooted at /files/...
 *
 * Security:
 * - UUID-based filenames (no user-controlled path traversal)
 * - Path normalization + containment check on fetch/delete
 * - Max file size controlled via Spring multipart config (see application.yaml)
 */
@Service
public class LocalStorageService implements StorageService {

    @Value("${storage.local.root:/var/hoi/uploads}")
    private String rootPath;

    private Path rootDir;

    @PostConstruct
    void init() {
        this.rootDir = Paths.get(rootPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootDir);
        } catch (IOException e) {
            throw new IllegalStateException("Không tạo được storage root: " + rootDir, e);
        }
    }

    @Override
    public String store(MultipartFile file, String pathPrefix) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File rỗng");
        }

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null) {
            int dot = original.lastIndexOf('.');
            if (dot >= 0 && dot < original.length() - 1) ext = original.substring(dot);
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        String cleanPrefix = normalizeSubpath(pathPrefix);
        Path target = rootDir.resolve(cleanPrefix).resolve(fileName).normalize();
        if (!target.startsWith(rootDir)) {
            throw new BusinessException("Đường dẫn không hợp lệ");
        }

        try {
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("Không lưu được file: " + e.getMessage());
        }

        return "/files/" + cleanPrefix + "/" + fileName;
    }

    @Override
    public Resource fetch(String url) {
        Path p = resolveUrlToPath(url);
        if (!Files.exists(p)) {
            throw new ResourceNotFoundException("File không tồn tại");
        }
        try {
            return new UrlResource(p.toUri());
        } catch (IOException e) {
            throw new BusinessException("Không đọc được file: " + e.getMessage());
        }
    }

    @Override
    public void delete(String url) {
        Path p = resolveUrlToPath(url);
        try {
            Files.deleteIfExists(p);
        } catch (IOException e) {
            throw new BusinessException("Không xoá được file: " + e.getMessage());
        }
    }

    // ===== helpers =====

    private String normalizeSubpath(String pathPrefix) {
        if (pathPrefix == null || pathPrefix.isBlank()) return "misc";
        String cleaned = pathPrefix.replace("\\", "/").replaceAll("^/+", "").replaceAll("/+$", "");
        if (cleaned.contains("..")) {
            throw new BusinessException("Đường dẫn không hợp lệ");
        }
        return cleaned;
    }

    private Path resolveUrlToPath(String url) {
        if (url == null || !url.startsWith("/files/")) {
            throw new BusinessException("URL file không hợp lệ");
        }
        String rel = url.substring("/files/".length());
        Path p = rootDir.resolve(rel).normalize();
        if (!p.startsWith(rootDir)) {
            throw new BusinessException("Đường dẫn không hợp lệ");
        }
        return p;
    }
}
