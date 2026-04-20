package manage.store.inventory.controller;

import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import manage.store.inventory.service.StorageService;

/**
 * File upload/download endpoints (G3, W7-9).
 * All paths under /api/files for axios baseURL + CORS consistency.
 *
 * Upload:   POST /api/files/upload?prefix=orders/42/contract
 *   → returns {"url": "/files/orders/42/contract/<uuid>.pdf"}
 * Download: GET /api/files/download?url=/files/orders/42/contract/<uuid>.pdf
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final StorageService storage;

    public FileController(StorageService storage) {
        this.storage = storage;
    }

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "prefix", defaultValue = "misc") String prefix) {
        String url = storage.store(file, prefix);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(@RequestParam("url") String url) {
        Resource res = storage.fetch(url);
        String filename = res.getFilename();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + (filename == null ? "file" : filename) + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(res);
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Void> delete(@RequestParam("url") String url) {
        storage.delete(url);
        return ResponseEntity.ok().build();
    }
}
