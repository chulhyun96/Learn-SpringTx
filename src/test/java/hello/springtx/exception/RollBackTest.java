package hello.springtx.exception;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
public class RollBackTest {

    @Autowired
    RollbackService service;


    @Test
    void runtimeException() {
        Assertions.assertThatThrownBy(() -> service.runtimeException())
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void checkException() {
        Assertions.assertThatThrownBy(() -> service.checkedException())
                .isInstanceOf(RollbackService.MyException.class);
    }

    @Test
    void rollBackFor() {
        Assertions.assertThatThrownBy(() -> service.rollbackFor())
                .isInstanceOf(RollbackService.MyException.class);
    }

    @Slf4j
    static class RollbackService {
         //런타임 예외 발생 시 : 롤백
        @Transactional
        public void runtimeException() {
            log.info("call runtimeExcpeiton");
            throw new RuntimeException();
        }

        // 체크 예외 발생 시 : 커밋
        @Transactional
        public void checkedException() throws MyException {
            log.info("call checkedException");
            throw new MyException();
        }

        @Transactional(rollbackFor = MyException.class)
        public void rollbackFor() throws MyException {
            log.info("call rollbackFor");
            throw new MyException();
        }

        static class MyException extends Exception {
        }
        // 체크 예외 rollbackFor 지정 : 롤백
    }
    @TestConfiguration
    static class RollBackTestConfig {
        @Bean
        RollbackService rollbackService() {
            return new RollbackService();
        }
    }
}
