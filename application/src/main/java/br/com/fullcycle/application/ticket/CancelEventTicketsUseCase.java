package br.com.fullcycle.application.ticket;

import br.com.fullcycle.application.UseCase;
import br.com.fullcycle.domain.event.EventId;
import br.com.fullcycle.domain.event.ticket.TicketRepository;
import br.com.fullcycle.domain.event.ticket.TicketStatus;

import java.util.Objects;

public class CancelEventTicketsUseCase
        extends UseCase<CancelEventTicketsUseCase.Input, CancelEventTicketsUseCase.Output> {

    private final TicketRepository ticketRepository;

    public CancelEventTicketsUseCase(final TicketRepository ticketRepository) {
        this.ticketRepository = Objects.requireNonNull(ticketRepository);
    }

    @Override
    public Output execute(final Input input) {
        final var ticketsToCancel = ticketRepository.ticketsByEventId(EventId.with(input.eventId())).stream()
                .filter(ticket -> !TicketStatus.CANCELLED.equals(ticket.status()))
                .toList();

        ticketsToCancel.forEach(ticket -> {
            ticket.cancel();
            ticketRepository.update(ticket);
        });

        return new Output(input.eventId(), ticketsToCancel.size());
    }

    public record Input(String eventId) {
    }

    public record Output(String eventId, int cancelledTickets) {
    }
}
