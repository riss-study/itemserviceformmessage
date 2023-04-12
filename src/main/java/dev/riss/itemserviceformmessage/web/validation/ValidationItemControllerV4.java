package dev.riss.itemserviceformmessage.web.validation;

import dev.riss.itemserviceformmessage.domain.item.*;
import dev.riss.itemserviceformmessage.web.validation.form.ItemSaveForm;
import dev.riss.itemserviceformmessage.web.validation.form.ItemUpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/validation/v4/items")
@RequiredArgsConstructor
@Slf4j
public class ValidationItemControllerV4 {

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
        return "validation/v4/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v4/addForm";
    }

    @PostMapping("/add")
    public String addItem(@ModelAttribute("item") @Validated ItemSaveForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // @ModelAttribute 안에 value 를 "item" 이라고 명시안해주면 model 에 "itemSaveForm" 이라는 이름으로 담김 (model.addAttribute("itemSaveForm", form))
        // BindingResult 에 에러메시지 담길때도 object name 이 ItemSaveForm 이 아닌 item 으로 담김 (에러메시지 대소문자 구분함)
        log.info("여기로 들어옴 V4");
        // 특정 필드가 아닌 복합 룰 검증
        if (null != form.getPrice() && null != form.getQuantity()) {
            int resultPrice = form.getPrice() * form.getQuantity();
            if (10000 > resultPrice) bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
        }

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.error("errors={}", bindingResult);
            // bindingResult 는 자동으로 view 에 담기므로, modelAttribute 에 담는 로직 생략 가능
            return "validation/v4/addForm";
        }

        // 검증 성공 로직
        Item item = new Item();

        item.setItemName(form.getItemName());
        item.setPrice(form.getPrice());
        item.setQuantity(form.getQuantity());
        item.setOpen(form.getOpen());
        item.setRegions(form.getRegions());
        item.setItemType(form.getItemType());
        item.setDeliveryCode(form.getDeliveryCode());

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }


    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute("item") ItemUpdateForm form, BindingResult bindingResult) {

        if (null != form.getPrice() && null != form.getQuantity()) {
            int resultPrice = form.getPrice() * form.getQuantity();
            if (10000 > resultPrice) bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
        }

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.error("errors={}", bindingResult);
            return "validation/v4/editForm";
        }

        Item itemParam = new Item();

        itemParam.setItemName(form.getItemName());
        itemParam.setPrice(form.getPrice());
        itemParam.setQuantity(form.getQuantity());
        itemParam.setOpen(form.getOpen());
        itemParam.setRegions(form.getRegions());
        itemParam.setItemType(form.getItemType());
        itemParam.setDeliveryCode(form.getDeliveryCode());

        itemRepository.update(itemId, itemParam);
        return "redirect:/validation/v4/items/{itemId}";
    }
}
