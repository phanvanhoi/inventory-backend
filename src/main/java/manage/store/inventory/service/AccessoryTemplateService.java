package manage.store.inventory.service;

import manage.store.inventory.dto.AccessoryTemplateCreateDTO;
import manage.store.inventory.dto.AccessoryTemplateDTO;

import java.util.List;

public interface AccessoryTemplateService {

    List<AccessoryTemplateDTO> getAll();

    AccessoryTemplateDTO getById(Long id);

    AccessoryTemplateDTO create(AccessoryTemplateCreateDTO dto, Long userId);

    AccessoryTemplateDTO update(Long id, AccessoryTemplateCreateDTO dto);

    void delete(Long id);
}
