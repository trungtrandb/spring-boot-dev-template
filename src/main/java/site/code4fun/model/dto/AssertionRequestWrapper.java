package site.code4fun.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yubico.webauthn.data.ByteArray;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;

@Value
public class AssertionRequestWrapper {

  String requestId;

  @JsonIgnore
  @NonNull com.yubico.webauthn.AssertionRequest request;
  String key;

  @SneakyThrows
  public AssertionRequestWrapper(
      @NonNull ByteArray requestId, @NonNull com.yubico.webauthn.AssertionRequest request) {
    this.requestId = requestId.getBase64();
    this.request = request;
    this.key = request.toCredentialsGetJson();
  }
}