package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import java.rmi.UnexpectedException;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired LogRepository logRepository;

    /*
    * memberService @Transactional : OFF
    * memberRepository @Transactional : ON
    * logRepository @Transactional : ON
    * 각각 별도의 트랜잭션 AOP를 통해 commit함*/

    @Test
    void outerTxOff_success() {
        // given
        String username = "outerTxOff_success";
        // when
        memberService.joinV1(username);
        // then
        Assertions.assertThat(memberRepository.find(username).isPresent()).isTrue();
        Assertions.assertThat(logRepository.find(username).isPresent()).isTrue();
    }
    /*
     * memberService @Transactional : OFF
     * memberRepository @Transactional : ON
     * logRepository @Transactional : ON  -> 로그예외 -> 롤백
     * 각각 별도의 트랜잭션을 AOP를 통해 실행되기 때문에 회원가입은 완료, 이력은 롤백됨*/
    @Test
    void outerTxOff_fail() {
        // given
        String username = "로그예외";
        // when
        Assertions.assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);
        // then
        Assertions.assertThat(logRepository.find(username).isPresent()).isFalse();
        Assertions.assertThat(memberRepository.find(username).isPresent()).isTrue();
    }

    @Test
    void singleTx() {
        // given
        String username = "singleTx";
        // when
        memberService.joinV1(username);
        // then
        Assertions.assertThat(memberRepository.find(username).isPresent()).isTrue();
        Assertions.assertThat(logRepository.find(username).isPresent()).isTrue();
    }

    @Test
    void outerTxOn_success() {
        // given
        String username = "outerTxOn_success";
        // when
        memberService.joinV1(username);
        // then
        Assertions.assertThat(memberRepository.find(username).isPresent()).isTrue();
        Assertions.assertThat(logRepository.find(username).isPresent()).isTrue();
    }

    /*
     * memberService @Transactional : OFF
     * memberRepository @Transactional : ON
     * logRepository @Transactional : ON Exception
     * */
    @Test
    void outerTxOn_fail() {
        // given
        String username = "로그예외_outerTxOn_fail";
        // when
        Assertions.assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);
        // then
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }
    @Test
    void recoverException_Fail() {
        // given
        String username = "로그예외_recoverException_Fail";
        // when
        Assertions.assertThatThrownBy(() -> memberService.joinV2(username))
                .isInstanceOf(UnexpectedRollbackException.class);
        // then
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }
    /*
     * memberService @Transactional : OFF
     * memberRepository @Transactional : ON
     * logRepository @Transactional : ON Exception -> 트랜잭션 분리
     * */
    @Test
    void recoverException_success() {
        // given
        String username = "로그예외_recoverException_success";
        // when
        memberService.joinV2(username);
        // then
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }

}