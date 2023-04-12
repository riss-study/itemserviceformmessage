package dev.riss.itemserviceformmessage.web.form;

import dev.riss.itemserviceformmessage.domain.item.DeliveryCode;
import dev.riss.itemserviceformmessage.domain.item.Item;
import dev.riss.itemserviceformmessage.domain.item.ItemRepository;
import dev.riss.itemserviceformmessage.domain.item.ItemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/form/items")
@RequiredArgsConstructor
@Slf4j
public class FormItemController {

    private final ItemRepository itemRepository;

    // 이 컨트롤러를 호출하면 항상 modelAttribute 에 "regions"이란 이름으로 해당 Map 객체(반환되는 객체)가 자동으로 model.addAttribute() 로 담김
    // 즉, 해당 컨트롤러의 어떤 요청이 호출돼든 자동으로 모델에 담겨서 해당 데이터를 model 에 넣는 코드를 요청 메서드마다 생략할 수 있음
    // 이렇게 하면, 호출할 때마다 계속 생성되기 때문에, 동적으로 변하는 값이 아니라면, 특정  static 영역에 생성해놓고 꺼내쓰는 게 최적화
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

    // 이 객체는 특히 모든 부분에서 쓰이는 게 아니기 때문에, 실제로는 따로 생성해놓고 꺼내쓰는 게 좋다
    @ModelAttribute("deliveryCodes")
    public List<DeliveryCode> deliveryCodes() {
        List<DeliveryCode> deliveryCodes = new ArrayList<>();
        deliveryCodes.add(new DeliveryCode("FAST", "빠른 배송"));
        deliveryCodes.add(new DeliveryCode("NORMAL", "일반 배송"));
        deliveryCodes.add(new DeliveryCode("SLOW", "느린 배송"));

        return deliveryCodes;
    }

    @GetMapping
    public String items (Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "form/items";
    }
    
    @GetMapping("/{itemId}")
    public String item (@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);

        return "form/item";
    }

    @GetMapping("/add")
    public String addForm (Model model) {
        model.addAttribute("item", new Item()); // form 에 연결하는 객체 => 폼 커맨드 객체

        return "form/addForm";
    }

    /*
//    @PostMapping("/add")
    public String addItemV1 (@RequestParam String itemName,
                        @RequestParam int price,
                        @RequestParam Integer quantity,
                        Model model) {
//        Item savedItem = itemRepository.save(new Item(itemName, price, quantity));
        Item item = new Item();
        item.setItemName(itemName);
        item.setPrice(price);
        item.setQuantity(quantity);

        Item savedItem = itemRepository.save(item);

        model.addAttribute("item", savedItem);

        return "form/item";
    }

//    @PostMapping("/add")
    public String addItemV2 (@ModelAttribute("item") Item item) {

        itemRepository.save(item);

//        model.addAttribute("item", savedItem);
        //@ModelAttribute 를 사용하면 value로 넣은 "item" 이름으로 저 item이 자동 추가가 되어
        // 이름이 같아서 addAttribute 는 생략이 가능하다

        return "form/item";
    }

//    @PostMapping("/add")
    public String addItemV3 (@ModelAttribute Item item) {

        itemRepository.save(item);

//        model.addAttribute("item", savedItem);
        //@ModelAttribute name 속성을 생략하면 클래스 명의 앞 글자를 소문자로 바꿔서 그 문자를 model 에 추가한다
        // 위는 그럼 Item -> item ==> "item" 을 model 에 추가 (변수명이 아닌 클래스명을 이용함)

        // ex. @ModelAttribute HelloData item -> model.attribute("helloData", item)

        return "form/item";
    }

//    @PostMapping("/add")
    public String addItemV4 (Item item) {

        itemRepository.save(item);
        // @ModelAttribute 는 생략 가능
        return "form/item";
    }

//    @PostMapping("/add")
    public String addItemV5 (Item item) {

        itemRepository.save(item);
        return "redirect:/form/items/"+item.getId();       //PRG 적용(Post/Redirect/Get) ==> V4처럼 뷰 템플릿을 호출하면
        // 마지막 요청이 POST 였기 때문에, 브라우저 새로고침하면 또 POST /form/items/add 요청이 오게 되어 같은 객체 파라미터로 새로운 아이템이 다시 만들어진다
        // 근데 이렇게 +item.getId() 로 redirect 하면 URL 인코딩이 안되므로 위험하다 뒤에서 설명하는 RedirectAttributes 를 사용하면 된다.
    }
*/

    // 사용자 입장에서 저장이 잘 된 건지 확인이 안됨(리다이렉트여서), 저장이 잘됐다는 문구의 메시지 보여달라는 요구사항이 왔을 때
    // RedirectAttributes 사용하자
    @PostMapping("/add")
    public String addItemV6 (@ModelAttribute Item item, RedirectAttributes redirectAttributes) {
        log.info("item.open={}", item.getOpen());
        log.info("item.regions={}", item.getRegions());
        log.info("item.itemType={}", item.getItemType());
        log.info("item.deliveryCode={}", item.getDeliveryCode());
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);    // 저장이 잘됐는지의 속성
        return "redirect:/form/items/{itemId}";    //RedirectAttributes 에 넣은 name 값이 model 에서 처럼 동작한다.(저 이름의 키를 가진 벨류로 치환됨)
        // RedirectAttributes 에 들어간 path parameter 에 치환되지 못하고 남은 attribute 들은 query parameter 로 들어가게 된다 (uri?key=value 형식으로)
    }


    @GetMapping("/{itemId}/edit")
    public String editForm (@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);

        return "form/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit (@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/form/items/{itemId}";    // {itemId} => 컨트롤러에 name value 와 매핑된 path variable 에 있는 값을 쓸 수 있게 해줌
    }

}
