package manage.store.inventory.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.ItemCreateDTO;
import manage.store.inventory.dto.ItemDetailDTO;
import manage.store.inventory.dto.ItemUpdateDTO;
import manage.store.inventory.entity.InventoryRequestItem;
import manage.store.inventory.entity.ProductVariant;
import manage.store.inventory.entity.enums.Gender;
import manage.store.inventory.repository.InventoryRequestItemRepository;
import manage.store.inventory.repository.ProductVariantRepository;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    private final InventoryRequestItemRepository itemRepository;
    private final ProductVariantRepository variantRepository;

    public ItemServiceImpl(
            InventoryRequestItemRepository itemRepository,
            ProductVariantRepository variantRepository
    ) {
        this.itemRepository = itemRepository;
        this.variantRepository = variantRepository;
    }

    @Override
    public Long createItem(ItemCreateDTO dto) {
        ProductVariant variant;

        // ITEM_BASED: variantId trực tiếp
        if (dto.getVariantId() != null) {
            variant = variantRepository.findById(dto.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found: id=" + dto.getVariantId()));
        }
        // STRUCTURED with style
        else if (dto.getStyleId() != null) {
            variant = variantRepository
                    .findVariant(dto.getStyleId(), dto.getSizeValue(), dto.getLengthCode())
                    .orElseThrow(() -> new RuntimeException(
                            "Variant not found: styleId=" + dto.getStyleId()
                                    + ", size=" + dto.getSizeValue()
                                    + ", length=" + dto.getLengthCode()
                    ));
        }
        // STRUCTURED with gender
        else if (dto.getGender() != null) {
            Gender gender = Gender.valueOf(dto.getGender());
            if (dto.getLengthCode() != null) {
                variant = variantRepository
                        .findStructuredVariantWithGenderAndLength(null, dto.getSizeValue(), dto.getLengthCode(), gender)
                        .orElseThrow(() -> new RuntimeException(
                                "Variant not found: size=" + dto.getSizeValue()
                                        + ", length=" + dto.getLengthCode()
                                        + ", gender=" + dto.getGender()
                        ));
            } else {
                variant = variantRepository
                        .findStructuredVariantWithGender(null, dto.getSizeValue(), gender)
                        .orElseThrow(() -> new RuntimeException(
                                "Variant not found: size=" + dto.getSizeValue()
                                        + ", gender=" + dto.getGender()
                        ));
            }
        } else {
            throw new RuntimeException("Không đủ thông tin để xác định variant");
        }

        InventoryRequestItem item = new InventoryRequestItem();
        item.setRequestId(dto.getRequestId());
        item.setVariantId(variant.getVariantId());
        item.setQuantity(dto.getQuantity());

        item = itemRepository.save(item);
        return item.getItemId();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDetailDTO getItemById(Long itemId) {
        return itemRepository.findItemDetailById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found: " + itemId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDetailDTO> getItemsByRequestId(Long requestId) {
        return itemRepository.findItemDetailsByRequestId(requestId);
    }

    @Override
    public ItemDetailDTO updateItem(Long itemId, ItemUpdateDTO dto) {
        InventoryRequestItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found: " + itemId));

        if (dto.getQuantity() != null && dto.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            item.setQuantity(dto.getQuantity());
        }

        itemRepository.save(item);

        return itemRepository.findItemDetailById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found: " + itemId));
    }

    @Override
    public void deleteItem(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new RuntimeException("Item not found: " + itemId);
        }
        itemRepository.deleteById(itemId);
    }
}
