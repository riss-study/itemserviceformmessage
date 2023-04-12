package dev.riss.itemserviceformmessage.web.validation;

import dev.riss.itemserviceformmessage.web.validation.form.ItemSaveForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/validation/api/items")
public class ValidationItemApiController {

    @PostMapping("/add")
    public Object addItem (@RequestBody @Validated ItemSaveForm form, BindingResult bindingResult) {
        // parsing 오류 즉, 이전의 TypeMissMatch 에서의 오류는 json(request body) 덩어리 자체를 객체로 바꾸질 못했기 때문에,
        // 컨트롤러 자체가 호출이 안되고, 예외가 터진다. (400 bad request)
        // json 으로 객체 변환에 실패하면 그냥 거기서 끝난거임. 컨트롤러 호출 못함. (나중에 예외처리에서 처리)
        // HttpMessageConverter 는 전체 객체 단위로 적용 (@ModelAttribute 는 HTTP 요청 파라미터를 처리하므로 각각 필드 단위로 정교하게 바인딩)
        // -> 메시지 컨버터 작동이 성공해야 Form 객체로 Parsing 되어 만들어지고 그 이후 @Valid, @validated 가 적용됨

        // API 의 경우 나타나는 3가지 case
        // case1: 성공 요청 -> 성공
        // case2: 실패 요청: JSON 을 객체로 생성(parsing)하는 것 자체 실패 (type 등으로 인한 convert 혹은 deserialization 실패)
        // case3: 검증 오류 요청: JSON 을 객체로 파싱하는 건 성공, but 검증(validation)에서 실패

        log.info("API Controller Call");

        if (bindingResult.hasErrors()) {
            log.info("Validation Error Occurred. errors={}", bindingResult);
            return bindingResult.getAllErrors();
        }

        log.info("Success. execute logic.");
        return form;
    }

}
