package br.com.fullcycle.application.ticket;

import br.com.fullcycle.application.repository.InMemoryTicketRepository;
import br.com.fullcycle.domain.customer.Customer;
import br.com.fullcycle.domain.event.Event;
import br.com.fullcycle.domain.event.ticket.Ticket;
import br.com.fullcycle.domain.event.ticket.TicketStatus;
import br.com.fullcycle.domain.partner.Partner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CancelEventTicketsUseCaseTest {

    @Test
    @DisplayName("Deve cancelar os tickets de um evento")
    public void testCancelEventTickets() throws Exception {
        // given
        final var aPartner = Partner.newPartner("John Doe", "41.536.538/0001-00", "john.doe@gmail.com");
        final var anEvent = Event.newEvent("Disney on Ice", "2021-01-01", 10, aPartner);
        final var otherEvent = Event.newEvent("Frozen on Ice", "2021-02-01", 10, aPartner);

        final var aCustomer = Customer.newCustomer("Gabriel Doe", "123.456.789-01", "gabriel.doe@gmail.com");
        final var otherCustomer = Customer.newCustomer("Pedro Doe", "123.111.789-01", "pedro.doe@gmail.com");

        final var ticket1 = Ticket.newTicket(aCustomer.customerId(), anEvent.eventId());
        final var ticket2 = Ticket.newTicket(otherCustomer.customerId(), anEvent.eventId());
        final var otherTicket = Ticket.newTicket(aCustomer.customerId(), otherEvent.eventId());

        final var ticketRepository = new InMemoryTicketRepository();
        ticketRepository.create(ticket1);
        ticketRepository.create(ticket2);
        ticketRepository.create(otherTicket);

        final var expectedEventId = anEvent.eventId().value();
        final var expectedCancelledTickets = 2;

        final var input = new CancelEventTicketsUseCase.Input(expectedEventId);

        // when
        final var useCase = new CancelEventTicketsUseCase(ticketRepository);
        final var output = useCase.execute(input);

        // then
        Assertions.assertEquals(expectedEventId, output.eventId());
        Assertions.assertEquals(expectedCancelledTickets, output.cancelledTickets());

        Assertions.assertEquals(TicketStatus.CANCELLED, ticketRepository.ticketOfId(ticket1.ticketId()).get().status());
        Assertions.assertEquals(TicketStatus.CANCELLED, ticketRepository.ticketOfId(ticket2.ticketId()).get().status());
        Assertions.assertEquals(TicketStatus.PENDING, ticketRepository.ticketOfId(otherTicket.ticketId()).get().status());
    }

    @Test
    @DisplayName("Deve cancelar zero tickets de um evento sem tickets")
    public void testCancelEventTicketsWithoutTickets() throws Exception {
        // given
        final var aPartner = Partner.newPartner("John Doe", "41.536.538/0001-00", "john.doe@gmail.com");
        final var anEvent = Event.newEvent("Disney on Ice", "2021-01-01", 10, aPartner);

        final var ticketRepository = new InMemoryTicketRepository();

        final var expectedEventId = anEvent.eventId().value();
        final var expectedCancelledTickets = 0;

        final var input = new CancelEventTicketsUseCase.Input(expectedEventId);

        // when
        final var useCase = new CancelEventTicketsUseCase(ticketRepository);
        final var output = useCase.execute(input);

        // then
        Assertions.assertEquals(expectedEventId, output.eventId());
        Assertions.assertEquals(expectedCancelledTickets, output.cancelledTickets());
    }

    @Test
    @DisplayName("Deve ser idempotente ao reprocessar o cancelamento dos tickets de um evento")
    public void testCancelEventTicketsIsIdempotent() throws Exception {
        // given
        final var aPartner = Partner.newPartner("John Doe", "41.536.538/0001-00", "john.doe@gmail.com");
        final var anEvent = Event.newEvent("Disney on Ice", "2021-01-01", 10, aPartner);

        final var aCustomer = Customer.newCustomer("Gabriel Doe", "123.456.789-01", "gabriel.doe@gmail.com");
        final var otherCustomer = Customer.newCustomer("Pedro Doe", "123.111.789-01", "pedro.doe@gmail.com");

        final var ticket1 = Ticket.newTicket(aCustomer.customerId(), anEvent.eventId());
        final var ticket2 = Ticket.newTicket(otherCustomer.customerId(), anEvent.eventId());

        final var ticketRepository = new InMemoryTicketRepository();
        ticketRepository.create(ticket1);
        ticketRepository.create(ticket2);

        final var expectedEventId = anEvent.eventId().value();

        final var input = new CancelEventTicketsUseCase.Input(expectedEventId);

        final var useCase = new CancelEventTicketsUseCase(ticketRepository);
        final var firstOutput = useCase.execute(input);

        Assertions.assertEquals(2, firstOutput.cancelledTickets());

        // when
        final var secondOutput = Assertions.assertDoesNotThrow(() -> useCase.execute(input));

        // then
        Assertions.assertEquals(expectedEventId, secondOutput.eventId());
        Assertions.assertEquals(0, secondOutput.cancelledTickets());

        Assertions.assertEquals(TicketStatus.CANCELLED, ticketRepository.ticketOfId(ticket1.ticketId()).get().status());
        Assertions.assertEquals(TicketStatus.CANCELLED, ticketRepository.ticketOfId(ticket2.ticketId()).get().status());
    }
}
