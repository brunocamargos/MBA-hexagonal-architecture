package br.com.fullcycle.infrastructure.gateways;

import br.com.fullcycle.IntegrationTest;
import br.com.fullcycle.domain.customer.CustomerId;
import br.com.fullcycle.domain.event.EventCancelled;
import br.com.fullcycle.domain.event.EventId;
import br.com.fullcycle.domain.event.ticket.Ticket;
import br.com.fullcycle.domain.event.ticket.TicketRepository;
import br.com.fullcycle.domain.event.ticket.TicketStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CancelEventTicketsIT extends IntegrationTest {

    @Autowired
    private ConsumerQueueGateway consumerQueueGateway;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve cancelar os tickets de um evento ao consumir o event.cancelled pela fila")
    public void testCancelEventTicketsThroughQueue() throws Exception {
        // given
        final var anEventId = EventId.unique();
        final var otherEventId = EventId.unique();

        final var ticket1 = createTicket(anEventId);
        final var ticket2 = createTicket(anEventId);
        final var otherTicket = createTicket(otherEventId);

        final var domainEvent = new EventCancelled(anEventId);

        // when
        consumerQueueGateway.publish(mapper.writeValueAsString(domainEvent));

        // then
        waitUntilAllCancelled(anEventId);

        Assertions.assertEquals(TicketStatus.CANCELLED, ticketRepository.ticketOfId(ticket1.ticketId()).get().status());
        Assertions.assertEquals(TicketStatus.CANCELLED, ticketRepository.ticketOfId(ticket2.ticketId()).get().status());
        Assertions.assertEquals(TicketStatus.PENDING, ticketRepository.ticketOfId(otherTicket.ticketId()).get().status());
    }

    private Ticket createTicket(final EventId anEventId) {
        return ticketRepository.create(Ticket.newTicket(CustomerId.unique(), anEventId));
    }

    private void waitUntilAllCancelled(final EventId anEventId) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            final var allCancelled = ticketRepository.ticketsByEventId(anEventId).stream()
                    .allMatch(it -> TicketStatus.CANCELLED.equals(it.status()));
            if (allCancelled) {
                return;
            }
            Thread.sleep(100);
        }
    }
}
