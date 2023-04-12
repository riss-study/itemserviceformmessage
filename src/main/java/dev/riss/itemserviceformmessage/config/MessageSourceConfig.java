package dev.riss.itemserviceformmessage.config;

//@Configuration
public class MessageSourceConfig {

    // 스프링부트가 MessageSource 를 자동으로 등록함
    // properties 파일 안에 spring.messages.basename=messages, config.i18n.messages 처럼 넣으면 됨
    // default 는 spring.messages.basename=messages 임 (messages 로 시작하는 프로퍼티 파일이 자동으로 등록되어 인식 가능)
/*    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource=new ResourceBundleMessageSource();
        messageSource.setBasenames("messages", "errors");   //messages.properties, errors.properties 파일(resources 폴더)을 자동으로 읽어서 사용
        messageSource.setDefaultEncoding("utf-8");
        return messageSource;
    }*/

}
