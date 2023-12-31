package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;


    @Test
    public void 상품주문() throws Exception{
        Member member = getMember();
        Book book = getBook("북1", 10000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        Order getOrder = orderRepository.findOne(orderId);

        Assertions.assertEquals(OrderStatus.ORDER, getOrder.getStatus(), "상품 주문 시 상태는 ORDER");
        Assertions.assertEquals(1, getOrder.getOrderItems().size(), "주문한 상품 종류 수가 일치");
        Assertions.assertEquals(orderCount * 10000, getOrder.getTotalPrice(), "주문가격은 가격 * 수량");
        Assertions.assertEquals(8, book.getStockQuantity(), "상품 주문 후 수량 변경");
    }

    @Test
    public void 주문취소() throws Exception{
        Member member = getMember();
        Book book = getBook("jpa", 10000 ,10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        Order getOrder = orderRepository.findOne(orderId);

        orderService.cancelOrder(orderId);

        Assertions.assertEquals(OrderStatus.CANCEL, getOrder.getStatus(), "상품 주문 취소 시 상태는 CANCEL");
        Assertions.assertEquals(10, book.getStockQuantity(), "상품 주문 취소 시 수 복구");
    }

    @Test
    public void 상품주문_재고수량초과() throws Exception{
        Member member = getMember();
        Book book = getBook("jpa", 10000 ,10);

        int orderCount = 11;

        Assertions.assertThrows(NotEnoughStockException.class, () -> orderService.order(member.getId(), book.getId(), orderCount));
    }

    private Book getBook(String name, int price, int quantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(quantity);
        em.persist(book);
        return book;
    }

    private Member getMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "경기", "123-123"));
        em.persist(member);
        return member;
    }
}
