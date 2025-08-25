package dev.minipay.payments.core;

import iso.std.iso._20022.tech.xsd.pain_001_001_11.*;
import jakarta.xml.bind.*;
import org.w3c.dom.Document;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import java.io.StringWriter;
import java.nio.file.Path;
import java.time.OffsetDateTime;

public class IsoXmlService {

  private final JAXBContext ctx;
  private final Schema pain001Schema; // optional validation
  private final ObjectFactory f = new ObjectFactory();

  public IsoXmlService(Path xsdDir) {
    try {
      this.ctx = JAXBContext.newInstance("dev.minipay.iso20022.pain001");
      if (xsdDir != null) {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        this.pain001Schema = sf.newSchema(xsdDir.resolve("pain.001.001.11.xsd").toFile());
      } else {
        this.pain001Schema = null;
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to init JAXB/Schema", e);
    }
  }

  public String buildPain001Xml(
      String debtorName, String debtorAcct, String creditorName, String creditorAcct,
      String ccy, String endToEndId, String instructedAmount, String reqExecDate) {

    // Root
    iso.std.iso._20022.tech.xsd.pain_001_001_11.Document root = f.createDocument();

    // <CstmrCdtTrfInitn>
    CstmrCdtTrfInitn cst = f.createCstmrCdtTrfInitn();
    root.setCstmrCdtTrfInitn(cst);

    // GrpHdr
    GroupHeader122 hdr = f.createGroupHeader122();
    hdr.setMsgId("PAY-" + endToEndId);
    hdr.setCreDtTm(OffsetDateTime.now());
    hdr.setNbOfTxs("1");
    PartyIdentification272 initg = f.createPartyIdentification272();
    initg.setNm("MiniPayments");
    hdr.setInitgPty(initg);
    cst.setGrpHdr(hdr);

    // PmtInf
    PaymentInstruction43 pi = f.createPaymentInstruction43();
    pi.setPmtInfId(endToEndId);
    pi.setPmtMtd(PaymentMethod3Code.TRF);
    pi.setReqdExctnDt(javax.xml.datatype.DatatypeFactory.newInstance()
        .newXMLGregorianCalendar(reqExecDate));
    PartyIdentification272 dbtr = f.createPartyIdentification272();
    dbtr.setNm(debtorName);
    pi.setDbtr(dbtr);
    CashAccount40 dbtrAcct = f.createCashAccount40();
    AccountIdentification4Choice dbtrAcctId = f.createAccountIdentification4Choice();
    GenericAccountIdentification1 othrDbtr = f.createGenericAccountIdentification1();
    othrDbtr.setId(debtorAcct);
    dbtrAcctId.setOthr(othrDbtr);
    dbtrAcct.setId(dbtrAcctId);
    pi.setDbtrAcct(dbtrAcct);

    // CdtTrfTxInf
    CreditTransferTransaction57 tx = f.createCreditTransferTransaction57();
    PaymentIdentification13 pmtId = f.createPaymentIdentification13();
    pmtId.setEndToEndId(endToEndId);
    tx.setPmtId(pmtId);

    AmountType4Choice amtChoice = f.createAmountType4Choice();
    ActiveOrHistoricCurrencyAndAmount instdAmt =
        f.createActiveOrHistoricCurrencyAndAmount();
    instdAmt.setCcy(ccy);
    instdAmt.setValue(new java.math.BigDecimal(instructedAmount));
    amtChoice.setInstdAmt(instdAmt);
    tx.setAmt(amtChoice);

    PartyIdentification272 cdtr = f.createPartyIdentification272();
    cdtr.setNm(creditorName);
    tx.setCdtr(cdtr);

    CashAccount40 cdtrAcct = f.createCashAccount40();
    AccountIdentification4Choice cdtrAcctId = f.createAccountIdentification4Choice();
    GenericAccountIdentification1 othrCdtr = f.createGenericAccountIdentification1();
    othrCdtr.setId(creditorAcct);
    cdtrAcctId.setOthr(othrCdtr);
    cdtrAcct.setId(cdtrAcctId);
    tx.setCdtrAcct(cdtrAcct);

    pi.getCdtTrfTxInf().add(tx);
    cst.getPmtInf().add(pi);

    // Marshal + (optional) XSD Validate
    try {
      Marshaller m = ctx.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      if (pain001Schema != null) m.setSchema(pain001Schema);

      // pretty DOM output
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      Document dom = dbf.newDocumentBuilder().newDocument();
      m.marshal(root, dom);

      StringWriter sw = new StringWriter();
      m.marshal(root, sw);
      return sw.toString();
    } catch (Exception e) {
      throw new RuntimeException("Marshalling failed", e);
    }
  }
}
