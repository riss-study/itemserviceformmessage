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
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
@Slf4j
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    // 해당 컨트롤러 요청될 때마다 WebDataBinder 가 내부적으로 만들어지고, 호출되면서 검증기를 넣어논다. (해당 컨트롤러의 모든 요청에서 다 적용됨)
    // WebDataBinder => 파라미터 바인딩을 해주는 객체 정도라고 생각
    // 컨트롤러에서 검증할 대상 객체에 @Validated 애노테이션 추가하면 됨
    @InitBinder
    public void init (WebDataBinder webDataBinder) {
        webDataBinder.addValidators(itemValidator);
    }

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
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

    //@PostMapping("/add")
    // parameter 순서 중요! BindingResult 를 넣을 @ModelAttribute 객체 파라미터 바로 뒤에 위치해야함
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        // 검증 로직
        if (!StringUtils.hasText(item.getItemName()))
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));

        if (null == item.getPrice() || 1000 > item.getPrice() || 1000000 < item.getPrice())
            bindingResult.addError(new FieldError("item", "price", "상품 가격은 1,000 ~ 1,000,000원까지 허용합니다."));

        if (null == item.getQuantity() || 9999 < item.getQuantity())
            bindingResult.addError(new FieldError("item", "quantity", "상품 수량은 최대 9,999개까지 허용합니다."));

        if (null == item.getItemType())
            bindingResult.addError(new FieldError("item", "itemType", "상품 종류를 선택해주세요."));

        if (0 == item.getRegions().size())
            bindingResult.addError(new FieldError("item", "regions", "상품 등록 지역을 하나 이상 선택해주세요"));

        if (item.getDeliveryCode().isBlank() || item.getDeliveryCode().isEmpty())
            bindingResult.addError(new FieldError("item", "deliveryCode", "상품 배송 방식을 선택해주세요."));

        // 특정 필드가 아닌 복합 룰 검증
        if (null != item.getPrice() && null != item.getQuantity()) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (10000 > resultPrice) bindingResult.addError(new ObjectError("item", "상품의 가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
        }

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.error("bindingResult={}", bindingResult);
            // bindingResult 는 자동으로 view 에 담기므로, modelAttribute 에 담는 로직 생략 가능
            //model.addAttribute("errors", errors);
            return "validation/v2/addForm";
        }

        // 검증 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        // 검증 로직
        // 기존 검증 로직은 binding 자체가 실패하면 fieldError 인 해당 field 값이 보존이 안됐음
        // why? ex. 타입 오류인 경우, 사용자 입력한 타입의 값을 보관할 수 없음(ex. price 는 Integer 이므로 String 타입을 보관할 수 없음)
        // 그러므로 해당 값을 보관하는 별도의 방법이 필요함. 그리고 해당 값을 검증 오류 발생시 화면에 다시 출력하도록 해야함
        // 보존되도록 수정함 (rejectedValue: 오류 발생 시 사용자가 입력한 값(거절된 값)을 저장하는 필드)
        // how? 바인딩에러가 발생하면 스프링에서 BindingResult 안에 rejectedValue 에 사용자가 입력한 거절된 값을 저장한 해당 FieldError 객체를 담는다.
        // (ex. bindingResult.addError(new FieldError("item", "itemName", "qqqq", true, null, null, "default message"));
        // 그럼 "qqqq" 같은 값이 담겨 있기 때문에 해당 fieldError 에서는 이 값을 꺼내서 쓸 수 있는 것임
        // (같은 objectName, field 의 fieldError 면 영향을 받는 각 field 에 대해 rejectedValue 가 노출된다고 스프링에 적혀있음)
        if (!StringUtils.hasText(item.getItemName()))   // 타입 오류 등의 binding 자체가 실패한 게 아닌 비즈니스 에러 이므로 bindingFailure 는 false 임. codes(에러 메시지 코드), arguments(메시지에서 사용하는 인자) 는 defaultMessage 를 대체하는 방법
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, null, null, "상품 이름은 필수입니다."));

        if (null == item.getPrice() || 1000 > item.getPrice() || 1000000 < item.getPrice())
            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false, null, null, "상품 가격은 1,000 ~ 1,000,000원까지 허용합니다."));

        if (null == item.getQuantity() || 9999 < item.getQuantity())
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, null, null, "상품 수량은 최대 9,999개까지 허용합니다."));

        if (null == item.getItemType())
            bindingResult.addError(new FieldError("item", "itemType", item.getItemType(), false, null, null, "상품 종류를 선택해주세요."));

        if (0 == item.getRegions().size())
            bindingResult.addError(new FieldError("item", "regions", item.getRegions(), false, null, null, "상품 등록 지역을 하나 이상 선택해주세요."));

        if (item.getDeliveryCode().isBlank() || item.getDeliveryCode().isEmpty())
            bindingResult.addError(new FieldError("item", "deliveryCode", item.getDeliveryCode(), false, null, null, "상품 배송 방식을 선택해주세요."));

        // 특정 필드가 아닌 복합 룰 검증
        if (null != item.getPrice() && null != item.getQuantity()) {
            int resultPrice = item.getPrice() * item.getQuantity(); // globalError 는 바인딘된 필드가 없기 때문에 들어갈 rejectValue 가 없음
            if (10000 > resultPrice) bindingResult.addError(new ObjectError("item", null, null, "상품의 가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
        }

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.error("bindingResult={}", bindingResult);
            // bindingResult 는 자동으로 view 에 담기므로, modelAttribute 에 담는 로직 생략 가능
            return "validation/v2/addForm";
        }

        // 검증 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //@PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        log.info("objectName={}", bindingResult.getObjectName());
        log.info("target={}", bindingResult.getTarget());

        if (!StringUtils.hasText(item.getItemName()))
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, new String[]{"required.item.itemName"}, null, null));

        if (null == item.getPrice() || 1000 > item.getPrice() || 1000000 < item.getPrice())
            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false, new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null));

        if (null == item.getQuantity() || 9999 < item.getQuantity())
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, new String[]{"max.item.quantity"}, new Object[]{99999}, null));

        if (null == item.getItemType())
            bindingResult.addError(new FieldError("item", "itemType", item.getItemType(), false, new String[]{"required.item.itemType"}, null, null));

        if (0 == item.getRegions().size())
            bindingResult.addError(new FieldError("item", "regions", item.getRegions(), false, new String[]{"required.item.regions"}, null, null));

        if (item.getDeliveryCode().isBlank() || item.getDeliveryCode().isEmpty())
            bindingResult.addError(new FieldError("item", "deliveryCode", item.getDeliveryCode(), false, new String[]{"required.item.deliveryCode"}, null, null));

        // 특정 필드가 아닌 복합 룰 검증
        if (null != item.getPrice() && null != item.getQuantity()) {
            int resultPrice = item.getPrice() * item.getQuantity(); // globalError 는 바인딘된 필드가 없기 때문에 들어갈 rejectValue 가 없음
            if (10000 > resultPrice) bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice}, null));
        }

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.error("bindingResult={}", bindingResult);
            // bindingResult 는 자동으로 view 에 담기므로, modelAttribute 에 담는 로직 생략 가능
            return "validation/v2/addForm";
        }

        // 검증 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //@PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        log.info("objectName={}", bindingResult.getObjectName());
        log.info("target={}", bindingResult.getTarget());

        // 검증 로직

        //ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "itemName", "required");
        // 아래의 text 가 존재하는지 이런 단순한 기능은 ValidationUtils 가 제공함 (복잡한 건 만들어서 사용하면 됨)
        if (!StringUtils.hasText(item.getItemName()))
            bindingResult.rejectValue("itemName", "required");
        // errorCode.objectName.field => messageCode 에서 찾음 (이렇게 규격화시켜서 메시지 프로퍼티 파일에 저장해논거임)
        // 프로퍼티 안에 errorCode 에 해당하는 key 만 있는 경우, 그 메시지 사용
        // errorCode.objectName.field 처럼 더 디테일한 key 가 같이 있는 경우, 더 디테일한 key 의 메시지 사용

        if (null == item.getPrice() || 1000 > item.getPrice() || 1000000 < item.getPrice())
            bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);

        if (null == item.getQuantity() || 9999 < item.getQuantity())
            bindingResult.rejectValue("quantity", "max", new Object[]{99999}, null);

        if (null == item.getItemType())
            bindingResult.rejectValue("itemType", "required");

        if (0 == item.getRegions().size())
            bindingResult.rejectValue("regions", "required");

        if (item.getDeliveryCode().isBlank() || item.getDeliveryCode().isEmpty())
            bindingResult.rejectValue("deliveryCode", "required");

        // 특정 필드가 아닌 복합 룰 검증
        if (null != item.getPrice() && null != item.getQuantity()) {
            int resultPrice = item.getPrice() * item.getQuantity(); // globalError 는 바인딘된 필드가 없기 때문에 들어갈 rejectValue 가 없음
            if (10000 > resultPrice) bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
        }

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.error("bindingResult={}", bindingResult);
            // bindingResult 는 자동으로 view 에 담기므로, modelAttribute 에 담는 로직 생략 가능
            return "validation/v2/addForm";
        }

        // 검증 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //@PostMapping("/add")
    public String addItemV5(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (itemValidator.supports(item.getClass())) {
            itemValidator.validate(item, bindingResult);
        }

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.error("bindingResult={}", bindingResult);
            // bindingResult 는 자동으로 view 에 담기므로, modelAttribute 에 담는 로직 생략 가능
            return "validation/v2/addForm";
        }

        // 검증 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    @PostMapping("/add")
    public String addItemV6(@ModelAttribute @Validated Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // @InitBinder->WebDataBinder 와 @Validated 를 통해 Validator 구현한 검증기 클래스의 supports, validate 메서드 자동호출해줌
        // 검증기를 자동으로 적용해줌
        // @Validated 가 붙으면 앞서 WebDataBinder 에 등록한 검증기(Validator) 를 찾아서 싫행한다.
        // ** @Valid: 자바 표준 검증 애노테이션(별도 라이브러리 의존관계 추가 필요), @Validated: 스프링 전용 검증 애노테이션 (차이가 잇지만, 둘중 아무거나 써도 되긴 함)
        // 만약 검증기가 여러 개면 어떤 검증기를 실행해야할지에 대한 구분이 필요 -> 이때 검증기 class 에 구현된 supports 메서드가 사용되는 것임
        // 이 과정을 자동으로 해준다

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.error("bindingResult={}", bindingResult);
            // bindingResult 는 자동으로 view 에 담기므로, modelAttribute 에 담는 로직 생략 가능
            return "validation/v2/addForm";
        }

        // 검증 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}
