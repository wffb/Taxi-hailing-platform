package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import model.Payment;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@AllArgsConstructor
@Data
public class WalletDTO {

    private BigDecimal balance;
    private List<Payment> records;
}
