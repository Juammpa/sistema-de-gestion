package org.sistemagestion.backend.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sistemagestion.backend.dto.payment.PaymentRequestDto;
import org.sistemagestion.backend.dto.payment.PaymentResponseDto;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.PaymentMapper;
import org.sistemagestion.backend.model.ButcherShop;
import org.sistemagestion.backend.model.Client;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.Payment;
import org.sistemagestion.backend.repository.ButcherShopRepository;
import org.sistemagestion.backend.repository.ClientRepository;
import org.sistemagestion.backend.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService implements IPaymentService{

    private final PaymentRepository paymentRepository;
    private final ClientRepository clientRepository;
    private final ButcherShopRepository butcherShopRepository;
    private final PaymentMapper mapper;

    @Override
    @Transactional
    public PaymentResponseDto createPayment(PaymentRequestDto request) {

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", request.getClientId()));

        ButcherShop butcherShop = butcherShopRepository.findById(request.getButcherShopId())
                .orElseThrow(() -> new ResourceNotFoundException("ButcherShop","id", request.getButcherShopId()));

        // Validamos si la sucursal es del cliente
        if(!butcherShop.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("The butcher shop does not belong to the given client.");
        }

        // Obtenemos la cuenta corriente de la sucursal
        CurrentAccount currentAccount = butcherShop.getCurrentAccount();

        Payment payment = mapper.toPayment(request,client,currentAccount);
        Payment paymentSaved = paymentRepository.save(payment);

        currentAccount.setCurrentBalance(payment.getBalance());

        log.info("Pago creado correctamente. ID: {}", paymentSaved.getId());
        return mapper.toDto(paymentSaved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getAllPaymentsByClient(Long clientId) {
        return paymentRepository.findByClientId(clientId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public PaymentResponseDto updatePayment(Long id, PaymentRequestDto request) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        CurrentAccount currentAccount = payment.getCurrentAccount();
        Double oldAmount = payment.getAmount();
        Double amountDifference = request.getAmount() - oldAmount;

        payment.setPaymentType(request.getPaymentType());
        payment.setAmount(request.getAmount());
        payment.setBalance(payment.getBalance() - amountDifference);

        currentAccount.setCurrentBalance(currentAccount.getCurrentBalance() - amountDifference);

        Payment paymentUpdated = paymentRepository.save(payment);
        log.info("Pago actualizado correctamente. ID: {}", payment.getId());

        return mapper.toDto(paymentUpdated);
    }

    @Override
    public void deletePaymentById(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment","id", id));

        // Actualizamos el balance de la cuenta
        CurrentAccount currentAccount = payment.getCurrentAccount();
        Double amount = payment.getAmount();
        currentAccount.setCurrentBalance(currentAccount.getCurrentBalance() + amount);

        paymentRepository.delete(payment);
        log.info("Pago eliminado corectamente. ID: {}", payment.getId());
    }
}
