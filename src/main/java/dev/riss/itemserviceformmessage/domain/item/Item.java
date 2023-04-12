package dev.riss.itemserviceformmessage.domain.item;

import lombok.*;

import java.util.List;

@Data
//@ScriptAssert(lang = "javascript", script = "_this.price * _this.quantity >= 10000") //java14 위 버젼부터는 lang="javascript" 지원 안함
// 애초에 제약이 많고 복잡한 애노테이션이라 오브젝트 오류는 직접 자바 코드로 작성하는 것을 권장
public class Item {

//    @NotNull(groups = UpdateCheck.class)    // 수정 폼 요구사항 추가
    private Long id;

//    @NotBlank(groups = {SaveCheck.class, UpdateCheck.class}, message = "공백 X") // 빈값(""), 공백만 있는 경우(" ") 비허용
    private String itemName;

//    @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
//    @Range(groups = {SaveCheck.class, UpdateCheck.class}, min = 1000, max = 1000000)
    private Integer price;      //int 로 하면 0이라도 들어가야 함, null 이 들어갈 가능성도 있기 때문에 primitive type 쓰지 않음

//    @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
//    @Max(value = 9999, groups = SaveCheck.class)        //수정 폼 요구사항 추가 (수정 때는 max 수량 제한 x)
    private Integer quantity;

    private Boolean open;       // 판매 여부

//    @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
//    @Size(groups = {SaveCheck.class, UpdateCheck.class}, min = 1, max = 3)
    private List<String> regions;    // 등록 지역

//    @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
    private ItemType itemType;      // 상품 종류

//    @NotBlank(groups = {SaveCheck.class, UpdateCheck.class})
    private String deliveryCode;    // 배송 방식    (상품 종류, 배송 방식은 비슷하나 예제를 위해 하난 enum, 하난 class 로 만듦)

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
