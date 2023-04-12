package dev.riss.itemserviceformmessage.validation;

import org.junit.jupiter.api.Test;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageCodesResolverTest {

    MessageCodesResolver codesResolver=new DefaultMessageCodesResolver();

    @Test
    void messageCodesResolverObject () {
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item");
        // new ObjectError("item", new String[]{"required.item", "required"}) << 이런 순서(디테일 -> 범용성)로 스프링이 해주는 거임
        assertThat(messageCodes).containsExactly("required.item", "required");
        // 순서: errorCode.objectName -> errorCode
    }

    @Test
    void messageCodesResolverField () {
        // 4번짜 인자는 해당 field 의 variable type
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item", "itemName", String.class);
        for (String messageCode : messageCodes) {
            System.out.println("messageCode = " + messageCode);
        }

        // bindingResult.rejectValue("itemName", "required") 를 하면 rejectValue 안에서 MessageCodesResolver 를  호출해서
        // new FieldError("item", "itemName", null, false, messageCodes, null, null); 를 직접해서 넘기는 것임
        // messageCodes => 위에서 resolveMessageCodes 로 만든 4 개의 메시지가 순서대로 들어가는 것임
        // new FieldError("item", "itemName", null, false, String[]{"required.item.itemName", "required.itemName", "required.java.lang.String", "required"}, null, null);
        // 순서: errorCode.objectName.field -> errorCode.field -> errorCode.fieldType(ex. "java.lang.String", "int") -> errorCode

        assertThat(messageCodes).containsExactly("required.item.itemName",
                "required.itemName",
                "required.java.lang.String",
                "required");
    }
}
