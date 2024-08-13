package hello.springtx.apply;

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

@Slf4j
@SpringBootTest
public class TxBasicTest {

    @Autowired
    BasicService basicService;

    @Test
    void proxyCheck() {
        log.info("aop class = {}", basicService.getClass());
        // 프록시가 적용되는지 보기 AopUtils.isAopProxy(basicService);
        boolean aopProxy = AopUtils.isAopProxy(basicService);
        log.info("aop proxy = {}", aopProxy);
        Assertions.assertThat(aopProxy).isTrue();
    }
    @Test
    void txText() {
        basicService.tx();
        basicService.nonTx();
    }


    @TestConfiguration
    static class TxApplyBasicConfig {
        @Bean
        BasicService basicService() {
            return new BasicService();
        }
    }
    @Slf4j
    static class BasicService {
        @Transactional
        public void tx(){
            log.info("Call BasicService.tx" );
            //Transaction이 적용 되는지 보기 TransactionSynchronizationManager.isActualTransactionActive()
            // Transaction이 적용이 되었다면 동적 프록시가 생성이 됨.
            boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active : {}", actualTransactionActive);
        }

        public void nonTx() {
            log.info("nonTx");
            boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active : {}", actualTransactionActive);
        }
    }
}
