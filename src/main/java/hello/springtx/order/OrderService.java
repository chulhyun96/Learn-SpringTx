package hello.springtx.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class  OrderService {
    private final OrderRepository repository;

    @Transactional
    public void order(Order order) throws NotEnoughMoneyException {
        log.info("call Order");
        repository.save(order);

        log.info("결제 프로세스 진입");
        if (order.getUserName().equals("예외")) {
            log.info("시스템 예외 발생");
            throw new RuntimeException("시스템 예외");
        } else if (order.getUserName().equals("잔고부족")) {
            log.info("잔고부족 비즈니스 예외 발생");
            order.setPayStatus("대기");
            throw new NotEnoughMoneyException("잔고부족");
        } else {
            //정상 승인
            log.info("정상 승인");
            order.setPayStatus("결제 완료");
        }
        log.info("결제 프로세스 완료");
    }
}
