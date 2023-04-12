package dev.riss.itemserviceformmessage.web.validation.form;

import dev.riss.itemserviceformmessage.domain.item.ItemType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.util.List;

@Data
public class ItemSaveForm {

    @NotBlank
    private String itemName;
    @NotNull
    @Range(min = 1000, max = 1000000)
    private Integer price;
    @NotNull
    @Max(value = 9999)
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
