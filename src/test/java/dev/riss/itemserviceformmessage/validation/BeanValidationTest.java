package dev.riss.itemserviceformmessage.validation;

import dev.riss.itemserviceformmessage.domain.item.Item;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Set;

public class BeanValidationTest {

    @Test
    void beanValidation () {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Item item = new Item();
        item.setItemName(null);
        item.setPrice(0);
        item.setQuantity(10000);
        item.setRegions(new ArrayList<>());
        item.setDeliveryCode(null);
        item.setItemType(null);

        System.out.println("item.getRegions().size() = " + item.getRegions().size());

        Set<ConstraintViolation<Item>> violations = validator.validate(item);
        for (ConstraintViolation<Item> violation : violations) {
            System.out.println("violation = " + violation);
            System.out.println("violation.getMessage() = " + violation.getMessage());
        }
    }
}
