package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.PaymentTransactionEntity;
import site.code4fun.model.dto.PaymentTransactionDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface PaymentTransactionMapper extends BaseMapper<PaymentTransactionEntity, PaymentTransactionDTO>{
}
