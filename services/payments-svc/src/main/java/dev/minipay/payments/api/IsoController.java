package dev.minipay.payments.api;

import dev.minipay.payments.api.dto.PaymentRequest;
import dev.minipay.payments.core.IsoXmlService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;

@RestController
@RequestMapping("/v1/iso")
public class IsoController {

  private final IsoXmlService svc;

  public IsoController() {
    // Pfad zu deinen XSDs (gleich wie beim Codegen)
    Path xsdDir = Path.of(System.getProperty("iso20022.xsd.dir",
        "../../schemas/iso20022"));
    this.svc = new IsoXmlService(xsdDir.toAbsolutePath().normalize());
  }

  @PostMapping(value = "/pain001/preview", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = "application/xml")
  public String preview(@Validated @RequestBody PaymentRequest req) {
    return svc.buildPain001Xml(
        req.debtorName(), req.debtorAccountId(),
        req.creditorName(), req.creditorAccountId(),
        req.currency(), req.endToEndId(),
        req.amount().toPlainString(),
        req.requestedExecutionDate().toString()
    );
  }
}
