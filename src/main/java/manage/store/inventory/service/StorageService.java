package manage.store.inventory.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Storage abstraction (G0-1 A3 decision, 2026-04-18).
 * Current impl: LocalStorageService (local disk VPS /var/hoi/uploads/).
 * Future: SwapR2StorageService / S3StorageService via @Primary or @Profile.
 */
public interface StorageService {

    /**
     * Store a file under a logical subpath and return a URL suitable for
     * later retrieval via {@link #fetch(String)}.
     *
     * @param file       uploaded multipart file
     * @param pathPrefix logical subpath e.g. "orders/42/contract"
     * @return relative URL (e.g. /files/orders/42/contract/abc123.pdf)
     */
    String store(MultipartFile file, String pathPrefix);

    /**
     * Fetch a previously stored file by URL.
     */
    Resource fetch(String url);

    /**
     * Delete a stored file. No-op if not found.
     */
    void delete(String url);
}
