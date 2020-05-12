package uk.gov.hmcts.reform.ethos.ecm.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.MultipleService;

@SpringBootApplication
@EnableCircuitBreaker
//@EnableScheduling
@EnableFeignClients(basePackages =
    {"uk.gov.hmcts.reform.ethos.ecm"
    })
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
@Slf4j
public class Application {

    public static void main(final String[] args) {
        //SpringApplication.run(Application.class, args);

        ApplicationContext applicationContext = SpringApplication.run(Application.class, args);
        log.info("Starting...");
        MultipleService service = applicationContext.getBean(MultipleService.class);
        service.sendUpdateToMultipleLogic();
        log.info("End");
    }
}