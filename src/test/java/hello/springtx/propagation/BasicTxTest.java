package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;

@Slf4j
@SpringBootTest
public class BasicTxTest {
    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager txManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }
    @Test
    void commit() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("트랜잭션 커밋 시작");
        txManager.commit(status);
        log.info("트랜잭션 커밋 완료");
    }
    @Test
    void rollback() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("트랜잭션 롤백 시작");
        txManager.rollback(status);
        log.info("트랜잭션 롤백 완료");
    }

    @Test
    void doubleCommit() {
        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("트랜잭션1 커밋");
        txManager.commit(tx1);

        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("트랜잭션2 롤백");
        txManager.rollback(tx2);
    }

    @DisplayName("물리 트랜잭션, 논리트랜잭션 모두 커밋")
    @Test
    void innerCommit() {
        log.info("==========외부 트랜잭션 시작==========");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("outer.isNewTransaction() = " + outer.isNewTransaction());

        log.info("==========내부 트랜잭션 시작==========");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("inner.isNewTransaction() = " + inner.isNewTransaction());
        log.info("==========내부 트랜잭션 커밋==========");
        txManager.commit(inner);

        log.info("==========외부 트랜잭션 커밋==========");
        txManager.commit(outer);
    }

    @DisplayName("외부 트랜잭션 롤백")
    @Test
    void outerRollback() {
        log.info("==========외부 트랜잭션 시작==========");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("outer.isNewTransaction() = " + outer.isNewTransaction());

        log.info("==========내부 트랜잭션 시작==========");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("inner.isNewTransaction() = " + inner.isNewTransaction());
        log.info("==========내부 트랜잭션 커밋==========");
        txManager.commit(inner);

        log.info("==========외부 트랜잭션 커밋==========");
        txManager.rollback(outer);
    }
    @DisplayName("내부 트랜잭션 롤백")
    @Test
    void innerRollback() {
        log.info("==========외부 트랜잭션 시작==========");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("outer.isNewTransaction() = " + outer.isNewTransaction());

        log.info("==========내부 트랜잭션 시작==========");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("inner.isNewTransaction() = " + inner.isNewTransaction());
        log.info("==========내부 트랜잭션 롤백==========");
        txManager.rollback(inner);

        log.info("==========외부 트랜잭션 커밋==========");
        Assertions.assertThatThrownBy(() -> txManager.commit(outer))
                .isInstanceOf(UnexpectedRollbackException.class);
    }

    @DisplayName("외부, 내부 트랜잭션 분리 REQUIRES_NEW")
    @Test
    void innerRollbackRequiresNew() {
        log.info("==========외부 트랜잭션 시작==========");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("outer.isNewTransaction() = " + outer.isNewTransaction());


        log.info("==========내부 트랜잭션 시작==========");
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        // 기본값은 기존 트랜잭션에 참가하는 것이지만,  PROPAGATION_REQUIRES_NEW 새로운 트랜잭션을 만들어버림
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        TransactionStatus inner = txManager.getTransaction(definition);
        log.info("inner.isNewTransaction() = " + inner.isNewTransaction());

        log.info("================내부 트랜잭션 롤백================");
        txManager.rollback(inner);

        log.info("==========외부 트랜잭션 커밋==========");
        txManager.commit(outer);
    }
}











