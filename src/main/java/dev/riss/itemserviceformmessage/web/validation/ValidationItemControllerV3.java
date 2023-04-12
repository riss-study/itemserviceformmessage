package dev.riss.itemserviceformmessage.web.validation;

import dev.riss.itemserviceformmessage.domain.item.*;
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
@RequestMapping("/validation/v3/items")
@RequiredArgsConstructor
@Slf4j
public class ValidationItemControllerV3 {

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
        return "validation/v3/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v3/addForm";
    }

    //@PostMapping("/add")
    public String addItem(@ModelAttribute @Validated Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // @Validated(@Valid 도 가능) -> Bean Validation 이 자동으로 적용 (spring-boot-starter-validation 라이브러리 덕분)
        // 스프링부트가 LocalValidatorFactoryBean 을 글로벌 Validator 로 등록함
        // (이전의 WebMvcConfigurer 클래스의 오버라이드로 글로벌 검증기 등록했으면, 스프링 부트가 Bean Validator 를 글로벌 검증기로 등록하지 않음)
        // 검증 대상 객체에 @Validated 가 있기 때문에, 해당 객체의 Bean Validation 을 보고 (객체 필드에 선언한 @NotBlank, @NotNull, @Size 등)
        // 검증 오류 발생하면 FieldError, ObjectError 생성해서 BindingResult 에 담아줌
        // -> 대신 아직은 에러코드를 넣은게 아니므로 errors.properties 에 있는 것이 아닌, 자카르타 혹은 하이버네이트에서 자동으로 만들어준 메시지나
        // 필드 변수의 Bean validation 에 등록한 메시지가 보임 (바인딩에서 자동으로 등록된 typeMismatch 는 프로퍼티로 적용한 메시지가 보임)
        // ==> @NotBlank 같은 애노테이션 이름이 오류코드로 등록됨 그러므로 이전의 오류코드로 등록한 메시지 적용이 안되는 거임 (에러코드를 바꾸면 됨)
        // 이것만 하면 당연히 기존에 만든 복합 검증은 빠짐

        // 에러메시지 적용 순서 (ValidationControllerV2 에서 배운 생성된 메시지 코드 순서대로 찾음) (ex. @NotBlank String itemName (Item class)
        // NotBlank.item.itemName -> NotBlank.itemName -> NotBlank -> defaultMessage (애노테이션의 message 속성에 등록한 메시지거나, 안햇으면 구현체 라이브러리에서 만들어주는 기본 메시지)

        // 검증 과정
        // 1. @ModelAttribute 로 인해 각 RequestParam 필드에 타입 변환 시도
        // 1.1. 타입 변환이 성공하면 데이터 바인딩 후 2번으로 감
        // 1.2. 실패하면 typeMismatch 로 FieldError 추가
        // 2. Validator 적용 (@ModelAttribute 로 바인딩이 성공한 필드만 BeanValidation 적용)
        // Bean Validation 이용하면 에러메시지에 parameter 를 직접 넣는 방법을 모르겠네...

        // 오브젝트 에러는 @ScriptAssert 보단 이렇게 직접 코드로 구현 권장
        // 특정 필드가 아닌 복합 룰 검증
        if (null != item.getPrice() && null != item.getQuantity()) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (10000 > resultPrice) bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
        }

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.error("errors={}", bindingResult);
            // bindingResult 는 자동으로 view 에 담기므로, modelAttribute 에 담는 로직 생략 가능
            return "validation/v3/addForm";
        }

        // 검증 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v3/items/{itemId}";
    }

    @PostMapping("/add")
    public String addItemV2(@ModelAttribute @Validated(SaveCheck.class) Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        // 특정 필드가 아닌 복합 룰 검증
        if (null != item.getPrice() && null != item.getQuantity()) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (10000 > resultPrice) bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
        }

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.error("errors={}", bindingResult);
            // bindingResult 는 자동으로 view 에 담기므로, modelAttribute 에 담는 로직 생략 가능
            return "validation/v3/addForm";
        }

        // 검증 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v3/items/{itemId}";
    }


    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/editForm";
    }

    //@PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute Item item, BindingResult bindingResult) {

        // 오브젝트 에러는 @ScriptAssert 보단 이렇게 직접 코드로 구현 권장
        // 특정 필드가 아닌 복합 룰 검증
        if (null != item.getPrice() && null != item.getQuantity()) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (10000 > resultPrice) bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
        }

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.error("errors={}", bindingResult);
            return "validation/v3/editForm";
        }

        itemRepository.update(itemId, item);
        return "redirect:/validation/v3/items/{itemId}";
    }

    @PostMapping("/{itemId}/edit")
    public String editV2(@PathVariable Long itemId, @Validated(UpdateCheck.class) @ModelAttribute Item item, BindingResult bindingResult) {
        // 실무에서는 @Validated 의 value 속성에 그룹 인터페이스 클래스를 지정해줘서 사용하는 이 groups 기능 사용 안함
        // 회원가입, 수정 할 때 넣는 데이터가 완전 달라서 다른 객체로 따로 받음 (나도 회원가입용 폼 객체, 수정용 dto 따로 만들어서 사용)

        // 오브젝트 에러는 @ScriptAssert 보단 이렇게 직접 코드로 구현 권장
        // 특정 필드가 아닌 복합 룰 검증
        if (null != item.getPrice() && null != item.getQuantity()) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (10000 > resultPrice) bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
        }

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.error("errors={}", bindingResult);
            return "validation/v3/editForm";
        }

        itemRepository.update(itemId, item);
        return "redirect:/validation/v3/items/{itemId}";
    }
}
