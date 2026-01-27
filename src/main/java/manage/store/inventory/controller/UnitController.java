package manage.store.inventory.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import manage.store.inventory.entity.Unit;
import manage.store.inventory.repository.UnitRepository;

@RestController
@RequestMapping("/api/units")
public class UnitController {

    private final UnitRepository unitRepository;

    public UnitController(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    // Lấy tất cả đơn vị
    @GetMapping
    public List<Unit> getAllUnits() {
        return unitRepository.findAll();
    }

    // Lấy đơn vị theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Unit> getUnitById(@PathVariable Long id) {
        return unitRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Tạo đơn vị mới
    @PostMapping
    public Unit createUnit(@RequestBody Unit unit) {
        return unitRepository.save(unit);
    }

    // Cập nhật đơn vị
    @PutMapping("/{id}")
    public ResponseEntity<Unit> updateUnit(@PathVariable Long id, @RequestBody Unit unitDetails) {
        return unitRepository.findById(id)
                .map(unit -> {
                    unit.setUnitName(unitDetails.getUnitName());
                    return ResponseEntity.ok(unitRepository.save(unit));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Xóa đơn vị
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUnit(@PathVariable Long id) {
        return unitRepository.findById(id)
                .map(unit -> {
                    unitRepository.delete(unit);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
