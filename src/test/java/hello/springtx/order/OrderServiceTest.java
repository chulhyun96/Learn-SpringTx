package hello.springtx.order;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
@Slf4j
class OrderServiceTest {
    @Autowired
    OrderService service;
    @Autowired
    OrderRepository repository;

    @Test
    void order() throws NotEnoughMoneyException {
        // given
        Order order = new Order();
        order.setUserName("정상");
        // when
        service.order(order);

        // then
        Order findOrder = repository.findById(order.getId()).get();
        Assertions.assertThat(findOrder.getPayStatus()).isEqualTo("결제 완료");
    }

    @Test
    void runtimeException() {
        // given
        Order order = new Order();
        order.setUserName("예외");
        // when
        // RuntimeException 발생 했기 때문에 데이터 rollback 결국 데이터가 없어야 된다.
        Assertions.assertThatThrownBy(() -> service.order(order))
                .isInstanceOf(RuntimeException.class);
        // then
        Optional<Order> findOrder = repository.findById(order.getId());
        Assertions.assertThat(findOrder.isEmpty()).isTrue();
    }

    @Test
    void bizException() {
        // given
        Order order = new Order();
        order.setUserName("잔고부족");
        // when
        try {
            service.order(order);
        } catch (NotEnoughMoneyException e) {
            log.info("고객에게 잔고 부족을 알리고 별도의 계좌로 입금하도록 안내");
        }
        // then
        Order findOrder = repository.findById(order.getId()).get();
        Assertions.assertThat(findOrder.getPayStatus()).isEqualTo("대기");
    }
}