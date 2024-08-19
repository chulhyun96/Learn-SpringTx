package hello.springtx.apply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;


@SpringBootTest
@Slf4j
public class InternalCallV1Test {
    @Autowired
    CallService callService;

    @Test
    void externalTest() {
        callService.external();
    }
    @Test
    void internalTest() {
        callService.internal();
    }
    @Test
    void proxyCheck() {
        log.info("proxyCheck = {}", callService.getClass().hashCode());
        log.info("proxyCheck = {}", callService.hashCode());
        log.info("callService getClass.equals(callService) : {}", callService.getClass().equals(callService));
        boolean aopProxy = AopUtils.isAopProxy(callService);
        Assertions.assertThat(aopProxy).isTrue();
    }

    @TestConfiguration
    static class InternalCallV1TestConfig {
        @Bean
        CallService callService() {
            return new CallService(internalService());
        }
        @Bean
        InternalService internalService() {
            return new InternalService();
        }
    }
    @Slf4j
    @RequiredArgsConstructor
    static class CallService {

        private final InternalService service;

        public void external() {
            log.info("call external");
            printTxInfo();
            //만약 여기서 internal을 호출 한다면?????
            /*service.internal();*/
            internal();
        }
        /*public void external() {
            log.info("call external");
            printTxInfo();
            //만약 여기서 internal을 호출 한다면?????
            internal();
        }*/
        @Transactional
        public void internal() {
            log.info("call internal");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("txActive: {}", txActive);
        }
    }

    // 내부 호출을 외부 호출로 변경
    static class InternalService {

        @Transactional
        public void internal() {
            log.info("call internal");
            printTxInfo();
        }
        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("txActive: {}", txActive);
        }
    }
}
