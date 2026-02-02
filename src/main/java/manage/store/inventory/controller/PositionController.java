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

import manage.store.inventory.entity.Position;
import manage.store.inventory.repository.PositionRepository;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    private final PositionRepository positionRepository;

    public PositionController(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    // Lấy tất cả chức danh
    @GetMapping
    public List<Position> getAllPositions() {
        return positionRepository.findAll();
    }

    // Lấy chức danh theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Position> getPositionById(@PathVariable Long id) {
        return positionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Tạo chức danh mới
    @PostMapping
    public Position createPosition(@RequestBody Position position) {
        return positionRepository.save(position);
    }

    // Cập nhật chức danh
    @PutMapping("/{id}")
    public ResponseEntity<Position> updatePosition(@PathVariable Long id, @RequestBody Position positionDetails) {
        return positionRepository.findById(id)
                .map(position -> {
                    position.setPositionCode(positionDetails.getPositionCode());
                    position.setPositionName(positionDetails.getPositionName());
                    return ResponseEntity.ok(positionRepository.save(position));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Xóa chức danh
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable Long id) {
        return positionRepository.findById(id)
                .map(position -> {
                    positionRepository.delete(position);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
