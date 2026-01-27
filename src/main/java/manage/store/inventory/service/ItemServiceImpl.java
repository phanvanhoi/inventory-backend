package manage.store.inventory.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.ItemCreateDTO;
import manage.store.inventory.dto.ItemDetailDTO;
import manage.store.inventory.dto.ItemUpdateDTO;
import manage.store.inventory.entity.InventoryRequestItem;
import manage.store.inventory.entity.ProductVariant;
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
        // Tìm variant
        ProductVariant variant = variantRepository
                .findVariant(dto.getStyleId(), dto.getSizeValue(), dto.getLengthCode())
                .orElseThrow(() -> new RuntimeException(
                        "Variant not found: styleId=" + dto.getStyleId()
                                + ", size=" + dto.getSizeValue()
                                + ", length=" + dto.getLengthCode()
                ));

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

        if (dto.getQuantity() != null && dto.getQuantity() > 0) {
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
