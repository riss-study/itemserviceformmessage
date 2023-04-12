package dev.riss.itemserviceformmessage.web.validation.form;

import dev.riss.itemserviceformmessage.domain.item.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.util.List;

@Data
public class ItemUpdateForm {

    @NotNull
    private Long id;

    @NotBlank
    private String itemName;
    @NotNull
    @Range(min = 1000, max = 1000000)
    private Integer price;

    // 수정시 수량은 자유롭게 변경 (null 도 가능)
    private Integer quantity;
    private Boolean open;

    @NotNull
    @Size(min = 1, max = 3)
    private List<String> regions;   // 등록 지역
    @NotNull
    private ItemType itemType;      // 상품 종류
    @NotBlank
    private String deliveryCode;    // 배송 방식

}
