package br.com.fullcycle.application.usecases;

import br.com.fullcycle.IntegrationTest;
import br.com.fullcycle.application.ticket.CancelEventTicketsUseCase;
import br.com.fullcycle.domain.customer.CustomerId;
import br.com.fullcycle.domain.event.EventId;
import br.com.fullcycle.domain.event.ticket.Ticket;
import br.com.fullcycle.domain.event.ticket.TicketRepository;
import br.com.fullcycle.domain.event.ticket.TicketStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CancelEventTicketsUseCaseIT extends IntegrationTest {

    @Autowired
    private CancelEventTicketsUseCase useCase;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve cancelar os tickets de um evento")
    public void testCancelEventTickets() throws Exception {
        // given
        final var anEventId = EventId.unique();
        final var otherEventId = EventId.unique();

        final var ticket1 = createTicket(anEventId);
        final var ticket2 = createTicket(anEventId);
        final var otherTicket = createTicket(otherEventId);

        final var expectedEventId = anEventId.value();
        final var expectedCancelledTickets = 2;

        final var input = new CancelEventTicketsUseCase.Input(expectedEventId);

        // when
        final var output = useCase.execute(input);

        // then
        Assertions.assertEquals(expectedEventId, output.eventId());
        Assertions.assertEquals(expectedCancelledTickets, output.cancelledTickets());

        Assertions.assertEquals(TicketStatus.CANCELLED, ticketRepository.ticketOfId(ticket1.ticketId()).get().status());
        Assertions.assertEquals(TicketStatus.CANCELLED, ticketRepository.ticketOfId(ticket2.ticketId()).get().status());
        Assertions.assertEquals(TicketStatus.PENDING, ticketRepository.ticketOfId(otherTicket.ticketId()).get().status());
    }

    @Test
    @DisplayName("Deve buscar os tickets de um evento")
    public void testTicketsByEventId() throws Exception {
        // given
        final var anEventId = EventId.unique();
        final var otherEventId = EventId.unique();

        final var ticket1 = createTicket(anEventId);
        final var ticket2 = createTicket(anEventId);
        createTicket(otherEventId);

        // when
        final var actualTickets = ticketRepository.ticketsByEventId(anEventId);

        // then
        Assertions.assertEquals(2, actualTickets.size());

        final var actualTicketIds = actualTickets.stream().map(Ticket::ticketId).toList();
        Assertions.assertTrue(actualTicketIds.contains(ticket1.ticketId()));
        Assertions.assertTrue(actualTicketIds.contains(ticket2.ticketId()));
    }

    private Ticket createTicket(final EventId anEventId) {
        return ticketRepository.create(Ticket.newTicket(CustomerId.unique(), anEventId));
    }
}
