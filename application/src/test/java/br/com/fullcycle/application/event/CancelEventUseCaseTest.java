package br.com.fullcycle.application.event;

import br.com.fullcycle.application.repository.InMemoryEventRepository;
import br.com.fullcycle.domain.event.Event;
import br.com.fullcycle.domain.event.EventId;
import br.com.fullcycle.domain.exceptions.ValidationException;
import br.com.fullcycle.domain.partner.Partner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CancelEventUseCaseTest {

    @Test
    @DisplayName("Deve cancelar um evento")
    public void testCancel() throws Exception {
        // given
        final var expectedStatus = "CANCELLED";

        final var aPartner = Partner.newPartner("John Doe", "41.536.538/0001-00", "john.doe@gmail.com");
        final var anEvent = Event.newEvent("Disney on Ice", "2021-01-01", 10, aPartner);

        final var eventID = anEvent.eventId().value();

        final var cancelInput = new CancelEventUseCase.Input(eventID);

        final var eventRepository = new InMemoryEventRepository();

        eventRepository.create(anEvent);

        // when
        final var useCase = new CancelEventUseCase(eventRepository);
        final var output = useCase.execute(cancelInput);

        // then
        Assertions.assertEquals(eventID, output.eventId());
        Assertions.assertEquals(expectedStatus, output.status());
    }

    @Test
    @DisplayName("Não deve cancelar um evento já cancelado")
    public void testCancelEventAlreadyCancelled() throws Exception {
        // given
        final var expectedError = "Event already cancelled";

        final var aPartner = Partner.newPartner("John Doe", "41.536.538/0001-00", "john.doe@gmail.com");
        final var anEvent = Event.newEvent("Disney on Ice", "2021-01-01", 10, aPartner);

        final var eventID = anEvent.eventId().value();

        final var cancelInput = new CancelEventUseCase.Input(eventID);

        final var eventRepository = new InMemoryEventRepository();

        eventRepository.create(anEvent);

        final var useCase = new CancelEventUseCase(eventRepository);
        useCase.execute(cancelInput);

        // when
        final var actualException = Assertions.assertThrows(ValidationException.class, () -> useCase.execute(cancelInput));

        // then
        Assertions.assertEquals(expectedError, actualException.getMessage());
    }

    @Test
    @DisplayName("Não deve cancelar um evento que não existe")
    public void testCancelEventThatDoesntExists() throws Exception {
        // given
        final var expectedError = "Event not found";

        final var eventID = EventId.unique().value();

        final var cancelInput = new CancelEventUseCase.Input(eventID);

        final var eventRepository = new InMemoryEventRepository();

        // when
        final var useCase = new CancelEventUseCase(eventRepository);
        final var actualException = Assertions.assertThrows(ValidationException.class, () -> useCase.execute(cancelInput));

        // then
        Assertions.assertEquals(expectedError, actualException.getMessage());
    }
}
