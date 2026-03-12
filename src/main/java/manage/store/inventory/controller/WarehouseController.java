package manage.store.inventory.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import manage.store.inventory.entity.Warehouse;
import manage.store.inventory.repository.WarehouseRepository;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseRepository warehouseRepository;

    public WarehouseController(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @GetMapping
    public List<Warehouse> getAll() {
        return warehouseRepository.findAll();
    }

    @GetMapping("/default")
    public Warehouse getDefault() {
        return warehouseRepository.findByIsDefaultTrue()
                .orElseThrow(() -> new RuntimeException("Default warehouse not found"));
    }

    @PostMapping
    public Warehouse create(@RequestBody Warehouse warehouse) {
        warehouse.setCreatedAt(LocalDateTime.now());
        if (warehouse.getIsDefault() == null) {
            warehouse.setIsDefault(false);
        }
        return warehouseRepository.save(warehouse);
    }

    @PutMapping("/{id}")
    public Warehouse update(@PathVariable Long id, @RequestBody Warehouse dto) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found: " + id));
        warehouse.setWarehouseName(dto.getWarehouseName());
        if (dto.getIsDefault() != null) {
            warehouse.setIsDefault(dto.getIsDefault());
        }
        return warehouseRepository.save(warehouse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found: " + id));
        if (Boolean.TRUE.equals(warehouse.getIsDefault())) {
            throw new IllegalArgumentException("Không thể xóa kho mặc định");
        }
        warehouseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
