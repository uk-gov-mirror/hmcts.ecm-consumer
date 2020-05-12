package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.ecm.common.model.bulk.SubmitBulkEvent;
import uk.gov.hmcts.reform.ethos.ecm.consumer.idam.ApiAccessToken;
import uk.gov.hmcts.reform.ethos.ecm.consumer.tasks.MultipleStateTask;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.*;

@Slf4j
@Service
public class MultipleService {

    private final CcdClient ccdClient;
    private final UserService userService;
    private static final String MESSAGE = "Failed to pull case: ";
    private static final String JURISDICTION = "EMPLOYMENT";
    private static final String CASE_TYPE_ID = SCOTLAND_BULK_CASE_TYPE_ID;
    private static final String USERNAME = "eric.ccdcooper@gmail.com";
    private static final String PASSWORD = "Nagoya0102";
    private static final String MULTIPLE_REFERENCE = "4150015";

    @Autowired
    public MultipleService(CcdClient ccdClient, UserService userService) {
        this.ccdClient = ccdClient;
        this.userService = userService;
    }

    //@Scheduled(fixedRate = 300000)
    public void sendUpdateToMultipleLogic() {
        log.info("Running after 5 minutes");
        String authToken = authenticateUser();
        //String authToken = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJrcmk1NjRrcmE1dXVkZGVsbGxwZDRvYWdpNCIsInN1YiI6Ijc2YjM5ODA0LTI4NTYtNDdlMS1iOTVmLTVjMTBhM2UwMTZjMCIsImlhdCI6MTU4OTI4NTc0MiwiY2FzZS1pZCI6IjEyNCIsImV2ZW50LWlkIjoidXBkYXRlQnVsa0FjdGlvbiIsImNhc2UtdHlwZS1pZCI6IlNjb3RsYW5kX011bHRpcGxlcyIsImp1cmlzZGljdGlvbi1pZCI6IkVNUExPWU1FTlQiLCJjYXNlLXN0YXRlIjoiQnVsa0Nhc2VTdGFydGVkIiwiY2FzZS12ZXJzaW9uIjoiMWExNDZlY2I1YmI0NGYxZDY4OGNhOTdhMGJjYjYxNjVlN2IxNTg0ZCIsImVudGl0eS12ZXJzaW9uIjoyMH0.NGC8nyXG2BT4zTyHnUkEaCRaVJmx_fgG0dxINywc3oM";
        List<SubmitBulkEvent> submitBulkEvents = retrieveMultipleCase(authToken);
        if (submitBulkEvents != null && !submitBulkEvents.isEmpty()) {

//            for (SubmitBulkEvent submitBulkEvent : submitBulkEvents) {
//                if (submitBulkEvent.getCaseData().getMultipleReference().equals(MULTIPLE_REFERENCE)) {
//                    log.info("submit BulkEvent: " + submitBulkEvent);
//                    sendUpdate(submitBulkEvent, authToken);
//                }
//            }

            log.info("submit BulkEvent: " + submitBulkEvents.get(0));
            sendUpdate(submitBulkEvents.get(0), authToken);
        } else {
            log.info("No submit events found");
        }
    }

    private String authenticateUser() {
        ApiAccessToken apiAccessToken = userService.loginUser(USERNAME, PASSWORD);
        log.info("API ACCESS TOKEN: " + apiAccessToken);
        return apiAccessToken.getAccessToken();
    }

    private List<SubmitBulkEvent> retrieveMultipleCase(String authToken) {
        try {
            return ccdClient.retrieveBulkCasesElasticSearch(authToken, CASE_TYPE_ID, MULTIPLE_REFERENCE);
            //return ccdClient.retrieveBulkCases(authToken, CASE_TYPE_ID, JURISDICTION);
        } catch (Exception ex) {
            throw new CaseCreationException(MESSAGE + MULTIPLE_REFERENCE + ex.getMessage());
        }
    }

    private void sendUpdate(SubmitBulkEvent submitBulkEvent, String authToken) {
        Instant start = Instant.now();
        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);
        executor.execute(new MultipleStateTask(JURISDICTION, CASE_TYPE_ID, submitBulkEvent, authToken, ccdClient));
        log.info("End in time: " + Duration.between(start, Instant.now()).toMillis());
        executor.shutdown();
    }
}