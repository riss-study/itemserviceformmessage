package dev.riss.itemserviceformmessage.web.validation;

import dev.riss.itemserviceformmessage.domain.item.DeliveryCode;
import dev.riss.itemserviceformmessage.domain.item.Item;
import dev.riss.itemserviceformmessage.domain.item.ItemRepository;
import dev.riss.itemserviceformmessage.domain.item.ItemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/validation/v1/items")
@RequiredArgsConstructor
@Slf4j
public class ValidationItemControllerV1 {

    private final ItemRepository itemRepository;

    @ModelAttribute("regions")
    public Map<String, String> regions() {
        Map<String, String> regions = new LinkedHashMap<>();
        regions.put("SEOUL", "서울");
        regions.put("BUSAN", "부산");
        regions.put("JEJU", "제주");

        return regions;
    }

    @ModelAttribute("itemTypes")
    public ItemType[] itemTypes() {
        return ItemType.values();
    }

    @ModelAttribute("deliveryCodes")
    public List<DeliveryCode> deliveryCodes() {
        List<DeliveryCode> deliveryCodes = new ArrayList<>();
        deliveryCodes.add(new DeliveryCode("FAST", "빠른 배송"));
        deliveryCodes.add(new DeliveryCode("NORMAL", "일반 배송"));
        deliveryCodes.add(new DeliveryCode("SLOW", "느린 배송"));

        return deliveryCodes;
    }

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v1/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v1/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v1/addForm";
    }

    @PostMapping("/add")
    public String addItem(@ModelAttribute Item item, RedirectAttributes redirectAttributes, Model model) {

        // 검증 오류 결과를 보관
        Map<String, String> errors = new HashMap<>();

        // 검증 로직
        if (!StringUtils.hasText(item.getItemName()))
            errors.put("itemName", "상품 이름은 필수입니다.");

        if (null == item.getPrice() || 1000 > item.getPrice() || 1000000 < item.getPrice())
            errors.put("price", "상품 가격은 1,000 ~ 1,000,000원까지 허용합니다.");

        if (null == item.getQuantity() || 9999 < item.getQuantity())
            errors.put("quantity", "상품 수량은 최대 9,999개까지 허용합니다.");

        if (null == item.getItemType())
            errors.put("itemType", "상품 종류를 선택해주세요.");

        if (0 == item.getRegions().size())
            errors.put("regions", "상품 등록 지역을 하나 이상 선택해주세요.");

        if (item.getDeliveryCode().isBlank() || item.getDeliveryCode().isEmpty())
            errors.put("deliveryCode", "상품 배송 방식을 선택해주세요.");
        
        // 특정 필드가 아닌 복합 룰 검증
        if (null != item.getPrice() && null != item.getQuantity()) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (10000 > resultPrice) errors.put("globalError", "상품의 가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice);
        }

        // 검증에 실패하면 다시 입력 폼으로
        if (!errors.isEmpty()) {
            log.error("errors={}", errors);
            model.addAttribute("errors", errors);
            // argument 에 @ModelAttribute 가 있음 -> 클래스가 Item 이므로 item 이라는 이름으로 해당 변수가 자동으로 모델에 추가됨
            // 그러므로 여기서 addForm 으로 이동했을 때, 기존의 값들이 유지된 것처럼 보여짐
            // (실제로는 post 로 받은 데이터 값을 그대로 model 에 넣었기 때문에 그 값들이 들어가서 노출됨)
            // 이렇게 해야 재사용이 가능하기 때문에, 기존의 addForm 에서 빈 Item 객체를 넣었던 거임 (Item 객체로 해당 폼 재사용)
            return "validation/v1/addForm";
        }

        // 검증 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v1/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v1/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v1/items/{itemId}";
    }

}
