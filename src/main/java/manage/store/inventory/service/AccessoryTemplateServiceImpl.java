package manage.store.inventory.service;

import manage.store.inventory.dto.AccessoryTemplateCreateDTO;
import manage.store.inventory.dto.AccessoryTemplateDTO;
import manage.store.inventory.entity.AccessoryTemplate;
import manage.store.inventory.entity.AccessoryTemplateItem;
import manage.store.inventory.entity.User;
import manage.store.inventory.repository.AccessoryTemplateRepository;
import manage.store.inventory.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AccessoryTemplateServiceImpl implements AccessoryTemplateService {

    private final AccessoryTemplateRepository templateRepo;
    private final UserRepository userRepo;

    public AccessoryTemplateServiceImpl(AccessoryTemplateRepository templateRepo,
                                        UserRepository userRepo) {
        this.templateRepo = templateRepo;
        this.userRepo = userRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccessoryTemplateDTO> getAll() {
        return templateRepo.findByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccessoryTemplateDTO getById(Long id) {
        AccessoryTemplate template = templateRepo.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Template not found: " + id));
        return toDTO(template);
    }

    @Override
    public AccessoryTemplateDTO create(AccessoryTemplateCreateDTO dto, Long userId) {
        AccessoryTemplate template = new AccessoryTemplate();
        template.setName(dto.getName());
        template.setCreatedBy(userId);
        template.setCreatedAt(LocalDateTime.now());
        applyItems(template, dto.getItems());
        return toDTO(templateRepo.save(template));
    }

    @Override
    public AccessoryTemplateDTO update(Long id, AccessoryTemplateCreateDTO dto) {
        AccessoryTemplate template = templateRepo.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Template not found: " + id));
        template.setName(dto.getName());
        template.getItems().clear();
        applyItems(template, dto.getItems());
        return toDTO(templateRepo.save(template));
    }

    @Override
    public void delete(Long id) {
        AccessoryTemplate template = templateRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found: " + id));
        template.setDeletedAt(LocalDateTime.now());
        templateRepo.save(template);
    }

    // ── helpers ──

    private void applyItems(AccessoryTemplate template, List<AccessoryTemplateCreateDTO.ItemDTO> itemDTOs) {
        if (itemDTOs == null) return;
        for (int i = 0; i < itemDTOs.size(); i++) {
            AccessoryTemplateCreateDTO.ItemDTO dto = itemDTOs.get(i);
            AccessoryTemplateItem item = new AccessoryTemplateItem();
            // templateId FK is managed by @JoinColumn on parent — do not set manually
            item.setVariantId(dto.getVariantId());
            item.setItemCode(dto.getItemCode());
            item.setItemName(dto.getItemName());
            item.setRate(dto.getRate());
            item.setUnit(dto.getUnit());
            item.setSortOrder(i);
            template.getItems().add(item);
        }
    }

    private AccessoryTemplateDTO toDTO(AccessoryTemplate template) {
        AccessoryTemplateDTO dto = new AccessoryTemplateDTO();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setCreatedAt(template.getCreatedAt());

        if (template.getCreatedBy() != null) {
            userRepo.findById(template.getCreatedBy())
                    .map(User::getFullName)
                    .ifPresent(dto::setCreatedByName);
        }

        List<AccessoryTemplateDTO.ItemDTO> itemDTOs = template.getItems().stream()
                .map(i -> {
                    AccessoryTemplateDTO.ItemDTO itemDto = new AccessoryTemplateDTO.ItemDTO();
                    itemDto.setId(i.getId());
                    itemDto.setVariantId(i.getVariantId());
                    itemDto.setItemCode(i.getItemCode());
                    itemDto.setItemName(i.getItemName());
                    itemDto.setRate(i.getRate());
                    itemDto.setUnit(i.getUnit());
                    itemDto.setSortOrder(i.getSortOrder());
                    return itemDto;
                })
                .toList();
        dto.setItems(itemDTOs);
        return dto;
    }
}
