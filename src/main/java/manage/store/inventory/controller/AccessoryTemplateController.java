package manage.store.inventory.controller;

import manage.store.inventory.dto.AccessoryTemplateCreateDTO;
import manage.store.inventory.dto.AccessoryTemplateDTO;
import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.service.AccessoryTemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accessory-templates")
public class AccessoryTemplateController {

    private final AccessoryTemplateService service;
    private final CurrentUser currentUser;

    public AccessoryTemplateController(AccessoryTemplateService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<AccessoryTemplateDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public AccessoryTemplateDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'PURCHASER')")
    public ResponseEntity<AccessoryTemplateDTO> create(@RequestBody AccessoryTemplateCreateDTO dto) {
        return ResponseEntity.ok(service.create(dto, currentUser.getUserId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'PURCHASER')")
    public ResponseEntity<AccessoryTemplateDTO> update(@PathVariable Long id,
                                                       @RequestBody AccessoryTemplateCreateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
