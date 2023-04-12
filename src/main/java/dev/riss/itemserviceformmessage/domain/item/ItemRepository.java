package dev.riss.itemserviceformmessage.domain.item;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ItemRepository {

    private static final Map<Long, Item> store=new HashMap<>(); // static, 실제로는 해쉬맵 말고 다른거 써야 함
    // 비동기에서는 static 이므로 동시에 접근 할 수 있으므로 ConcurrentHashMap 써야 함
    private static long sequence=0L;    // static
    // 얘도 비동기에서는 AtomicLong 써야 함

    public Item save (Item item) {
        item.setId(++sequence);
        store.put(item.getId(), item);
        return item;
    }

    public Item findById (Long id) {
        return store.get(id);
    }

    public List<Item> findAll () {
        return new ArrayList<>(store.values()); // ArrayList 등으로 한번 감싸서 반환하면,
        // 해당 데이터에 새로운 값을 넣어도 실제 store 에 반영이 안되기 때문에
        // 안전하게 감싸서 반환함
    }

    public void update (Long itemId, Item updateParam) {
        Item findItem = findById(itemId);
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
        findItem.setOpen(updateParam.getOpen());
        findItem.setRegions(updateParam.getRegions());
        findItem.setItemType(updateParam.getItemType());
        findItem.setDeliveryCode(updateParam.getDeliveryCode());
    }

    public void clearStore () {
        store.clear();
    }

}
