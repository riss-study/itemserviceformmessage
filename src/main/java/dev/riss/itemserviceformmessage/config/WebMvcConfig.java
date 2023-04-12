package dev.riss.itemserviceformmessage.config;

import dev.riss.itemserviceformmessage.web.validation.ItemValidator;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 모든 컨트롤러에서 Validator 호출 하도록 설정하는 방법 (이런 방법은 거의 드묾)
// 이거 해놓으면 @InitBinder 빼도 됨
//@Component <-- 다음 강의를 위해 주석처리 해놈 (글로벌 설정하면 BeanValidator 자동 등록이 안됨)
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public Validator getValidator () {
        return new ItemValidator();
    }

}
