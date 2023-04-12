package dev.riss.itemserviceformmessage.web.validation;

import dev.riss.itemserviceformmessage.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ItemValidator implements Validator {

    // 해당 검증기를 지원하는 지 여부 확인
    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
        // isAssignableFrom -> item == clazz
        // item == subItem (자식 클래스여도 통과하는 메서드)
    }

    // 검증 대상 객체와 BindingResult 넣어서 검증 후 FieldError, GlobalError 넣어줌
    @Override
    public void validate(Object target, Errors bindingResult) {    //target -> item, errors -> Errors(BindingResult 의 부모 클래스)
        Item item= (Item) target;

        // 검증 로직

        if (!StringUtils.hasText(item.getItemName()))
            bindingResult.rejectValue("itemName", "required");

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
            int resultPrice = item.getPrice() * item.getQuantity();
            if (10000 > resultPrice) bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
        }
    }
}
