package songoycollection.depense.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import songoycollection.article.repository.ArticleVariantRepository;
import songoycollection.depense.dtos.ReceptionRequest;
import songoycollection.depense.model.Depense;
import songoycollection.achat.model.Achat;
import songoycollection.personne.model.Personnel;
import songoycollection.personne.shared.PersonnelUtiliShared;
import songoycollection.shared.EnumShared;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReceptionHelperServiceTest {
    private final EntityManager entityManager = mock(EntityManager.class);
    private final ReceptionHelperService helper = new ReceptionHelperService(
            entityManager, mock(PersonnelUtiliShared.class), mock(ArticleVariantRepository.class)
    );

    private ReceptionRequest request(List<ReceptionRequest.Ligne> lines) {
        return new ReceptionRequest(
                LocalDate.now(), EnumShared.Entite.BOUTIQUE, null, "Marchand",
                null, lines, null
        );
    }

    @Test
    void acceptsDecimalQuantitiesAndRoundsEachLine() {
        var request = request(List.of(
                new ReceptionRequest.Ligne(1L, 2.5, 101.0),
                new ReceptionRequest.Ligne(2L, 1.5, 101.0)
        ));
        assertDoesNotThrow(() -> helper.validateRequest(request));
        assertEquals(405.0, helper.calculateTotal(request));
    }

    @Test
    void rejectsDuplicateVariantsAndNonFiniteQuantity() {
        var line = new ReceptionRequest.Ligne(1L, 2.0, 100.0);
        assertThrows(IllegalArgumentException.class,
                () -> helper.validateRequest(request(List.of(line, line))));
        assertThrows(IllegalArgumentException.class,
                () -> helper.validateRequest(request(List.of(
                        new ReceptionRequest.Ligne(1L, Double.POSITIVE_INFINITY, 100.0)
                ))));
    }

    @Test
    void acceptsFullCreditWithoutCreatingExpense() {
        assertEquals(0.0, helper.validatePayment(null, 100000.0));
        var credit = new ReceptionRequest.Paiement(0.0, EnumShared.ModePaiement.CREDIT);
        assertEquals(0.0, helper.validatePayment(credit, 100000.0));
        helper.createPayment(new Achat(), credit, new Personnel());
        verifyNoInteractions(entityManager);
    }

    @Test
    void partialPaymentCreatesOnlyThePaidExpense() {
        var payment = new ReceptionRequest.Paiement(40000.0, EnumShared.ModePaiement.MOBILE);
        assertEquals(40000.0, helper.validatePayment(payment, 100000.0));
        Achat achat = new Achat();
        helper.createPayment(achat, payment, new Personnel());
        var captor = org.mockito.ArgumentCaptor.forClass(Depense.class);
        verify(entityManager).persist(captor.capture());
        assertSame(achat, captor.getValue().getAchat());
        assertEquals(40000.0, captor.getValue().getMontant());
        assertEquals(EnumShared.ModePaiement.MOBILE, captor.getValue().getModePaiement());
    }

    @Test
    void rejectsOverpaymentAndCreditAsAnActualTransfer() {
        assertThrows(IllegalArgumentException.class, () -> helper.validatePayment(
                new ReceptionRequest.Paiement(100001.0, EnumShared.ModePaiement.CASH), 100000.0
        ));
        assertThrows(IllegalArgumentException.class, () -> helper.validatePayment(
                new ReceptionRequest.Paiement(40000.0, EnumShared.ModePaiement.CREDIT), 100000.0
        ));
    }

    @Test
    void freeSupplierDoesNotCreateSupplierRecord() {
        assertNull(helper.resolveFournisseur(request(List.of())));
        verifyNoInteractions(entityManager);
        var invalid = new ReceptionRequest(
                LocalDate.now(), EnumShared.Entite.BOUTIQUE, 1L, "Marchand", null, List.of(), null
        );
        assertThrows(IllegalArgumentException.class, () -> helper.resolveFournisseur(invalid));
    }
}
