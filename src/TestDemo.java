import com.fasterxml.jackson.databind.ObjectMapper;
import ipworks3ds.*;

import java.util.Map;

public class TestDemo {


    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.config("ProtocolVersion=2.2.0");

        String cacertsPath = System.getProperty("java.home") + "\\lib\\security\\cacerts";
        System.out.println("Java cacerts: " + cacertsPath);

        try {
            System.out.println(server.getServerTransactionId());

            server.setMethodNotificationURL("http://www.processing.kz");
            System.out.println(server.getMethodData());
            System.out.println(server.getServerTransactionId());

            testMcMtf(server);
            //testVisaProd(server);
            //testVsts(server);
            //testN(server);

            System.out.println("test done.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(server.getDataPacketOut());
            System.out.println(server.getErrorPacket());
            e.printStackTrace();
        }
    }

    public static void testMcMtf(Server server) throws Exception {
        server.setDirectoryServerURL("https://mtf.3ds2.directory.mastercard.com/mtf/3ds/ds2/svc");

        //server.config("ServerRefNumber=3DS_LOA_SER_NSOF_020200_00416");
        server.config("ServerRefNumber=3DS_LOA_SER_PPFU_020100_00008");

        server.config("ServerOperatorId=threeDSServerOperatorUL");

        initListener(server);

        initMcMtf(server);

        System.out.println(server.getSSLCert().getSubject());
        System.out.println(server.getSSLAcceptServerCert().getSubject());

        System.out.println("Sending areq...");
        testMcAReq(server, getPArq());

        //getCardRanges(server);

        System.out.println(server.getTransactionStatus());
        System.out.println(server.getAuthenticationECI());
        System.out.println(server.getAuthenticationValue());

        System.out.println(server.getDataPacketOut());
        System.out.println(server.getErrorPacket());
    }

    private static Map<String, Object> getPPrq() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(pArq.BODY_PREQ, Map.class);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private static Map<String, Object> getPArq() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(pArq.BODY_BRW_01, Map.class);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private static void prepareDataForApp(Server server, Map<String, Object> request) throws Exception {
        server.setDeviceChannel(request.get("deviceChannel").toString());

        Client sdk = new Client();
        initMcClientMtf(sdk);

        sdk.setSDKAppId(request.get("sdkAppID").toString());
        sdk.config("SDKReferenceNumber=" + request.get("sdkReferenceNumber"));
        sdk.setSDKTransactionId(request.get("sdkTransID").toString());
        sdk.addDeviceParam("", "1.1", 2, 0);
        String call = sdk.getAuthRequest();

        System.out.println(call);
        server.setClientAuthRequest(call);
    }

    private static void testMcAReq(Server server, Map<String, Object> request) throws Exception {
        server.config("ProtocolVersion=" + request.get("messageVersion").toString());
        server.config("MethodCompletionIndicator=" + request.get("threeDSCompInd").toString());

        server.setAccountType(request.get("acctType").toString());
        server.config("AddressMatch=" + request.get("addrMatch").toString());
        server.config("AccountId=" + request.get("acctID").toString());

        server.config("RequestorChallengeInd=" + request.get("threeDSRequestorChallengeInd").toString());
        server.config("DecoupledRequestIndicator=" + request.get("threeDSRequestorDecReqInd").toString());


        server.setCardholderName(request.get("cardholderName").toString());
        Map<String, Object> workPhone = (Map<String, Object>) request.get("workPhone");
        server.setCardholderWorkPhone(workPhone.get("cc").toString() + "-" + workPhone.get("subscriber").toString());
        Map<String, Object> homePhone = (Map<String, Object>) request.get("homePhone");
        server.setCardholderHomePhone(homePhone.get("cc").toString() + "-" + homePhone.get("subscriber").toString());
        Map<String, Object> mobilePhone = (Map<String, Object>) request.get("mobilePhone");
        server.setCardholderMobilePhone(mobilePhone.get("cc").toString() + "-" + mobilePhone.get("subscriber").toString());
        server.setCardholderEmail(request.get("email").toString());

        AddressInfo billing = new AddressInfo();
        billing.setPostalCode(request.get("billAddrPostCode").toString());
        billing.setCountry(request.get("billAddrCountry").toString());
        billing.setState(request.get("billAddrState").toString());
        billing.setCity(request.get("billAddrCity").toString());
        billing.setLine1(request.get("billAddrLine1").toString());
        billing.setLine2(request.get("billAddrLine2").toString());
        billing.setLine3(request.get("billAddrLine3").toString());
        server.setBillingAddress(billing);

        AddressInfo shipping = new AddressInfo();
        shipping.setPostalCode(request.get("shipAddrPostCode").toString());
        shipping.setCountry(request.get("shipAddrCountry").toString());
        shipping.setState(request.get("shipAddrState").toString());
        shipping.setCity(request.get("shipAddrCity").toString());
        shipping.setLine1(request.get("shipAddrLine1").toString());
        shipping.setLine2(request.get("shipAddrLine2").toString());
        shipping.setLine3(request.get("shipAddrLine3").toString());
        server.setShippingAddress(shipping);

        Map<String, Object> authInfo = (Map<String, Object>) request.get("threeDSRequestorAuthenticationInfo");
        server.config("ReqAuthMethod=" + authInfo.get("threeDSReqAuthMethod").toString());
        server.config("ReqAuthTimestamp=" + authInfo.get("threeDSReqAuthTimestamp").toString());
        server.config("ReqAuthData=" + authInfo.get("threeDSReqAuthData").toString());

        Map<String, Object> accInfo = (Map<String, Object>) request.get("acctInfo");
        server.config("AccountAgeIndicator=" + accInfo.get("chAccAgeInd").toString());
        server.config("AccountDate=" + accInfo.get("chAccDate").toString());
        server.config("AccountChangeIndicator=" + accInfo.get("chAccChangeInd").toString());
        server.config("AccountChangeDate=" + accInfo.get("chAccChange").toString());
        server.config("AccountPasswordChangeIndicator=" + accInfo.get("chAccPwChangeInd").toString());
        server.config("AccountPasswordChangeDate=" + accInfo.get("chAccPwChange").toString());
        server.config("ShipAddressUsageIndicator=" + accInfo.get("shipAddressUsageInd").toString());
        server.config("ShipAddressUsageDate=" + accInfo.get("shipAddressUsage").toString());
        server.config("AccountDayTransactions=" + accInfo.get("txnActivityDay").toString());
        server.config("AccountYearTransactions=" + accInfo.get("txnActivityYear").toString());
        server.config("AccountProvisioningAttempts=" + accInfo.get("provisionAttemptsDay").toString());
        server.config("AccountPurchaseCount=" + accInfo.get("nbPurchaseAccount").toString());
        server.config("SuspiciousAccountActivity=" + accInfo.get("suspiciousAccActivity").toString());
        server.config("ShipNameIndicator=" + accInfo.get("shipNameIndicator").toString());
        server.config("PaymentAccountAgeIndicator=" + accInfo.get("paymentAccInd").toString());
        server.config("PaymentAccountAge=" + accInfo.get("paymentAccAge").toString());

        Map<String, Object> riskInfo = (Map<String, Object>) request.get("merchantRiskIndicator");
        server.config("ShipIndicator=" + riskInfo.get("shipIndicator").toString());
        server.config("DeliveryTimeframe=" + riskInfo.get("deliveryTimeframe").toString());
        server.config("DeliveryEmailAddress=" + riskInfo.get("deliveryEmailAddress").toString());
        server.config("ReorderItemsIndicator=" + riskInfo.get("reorderItemsInd").toString());
        server.config("PreOrderPurchaseIndicator=" + riskInfo.get("preOrderPurchaseInd").toString());
        server.config("PreOrderDate=" + riskInfo.get("preOrderDate").toString());
        server.config("GiftCardAmount=" + riskInfo.get("giftCardAmount").toString());
        server.config("GiftCardCurrency=" + riskInfo.get("giftCardCurr").toString());
        server.config("GiftCardCount=" + riskInfo.get("giftCardCount").toString());


        String channel = request.get("deviceChannel").toString();

        System.out.println(channel);
        if ("01".equals(channel)) {
            System.out.println("prepare app data...");
            prepareDataForApp(server, request);
        } else if ("03".equals(channel)) {
            System.out.println("prepare ri data...");
            server.config("ThreeRIIndicator=06");
            server.config("RequestorChallengeInd=06");
        } else {
            System.out.println("prepare browser data...");
        }

        server.setPurchaseAmount(request.get("purchaseAmount").toString());
        server.setMessageCategory(request.get("messageCategory").toString());
        server.setRequestorId(request.get("threeDSRequestorID").toString());
        server.setRequestorName(request.get("threeDSRequestorName").toString());
        server.setRequestorURL(request.get("threeDSRequestorURL").toString());
        server.setAcquirerBIN("555555");

        server.setAcquirerMerchantId(request.get("acquirerMerchantID").toString());

        server.setMerchantCategoryCode(request.get("mcc").toString());
        server.setMerchantCountryCode(request.get("purchaseCurrency").toString());
        server.setMerchantName(request.get("merchantName").toString());
        server.setPurchaseDate(request.get("purchaseDate").toString());

        server.setResultsURL("https://www.processing.kz");

        server.setNotificationURL(request.get("notificationURL").toString());
        server.setBrowserJavaEnabledVal(request.get("browserJavaEnabled").toString().equals("true")? 1:2);
        server.setBrowserJavaScriptEnabledVal(request.get("browserJavascriptEnabled").toString().equals("true")? 1:2);
        server.setBrowserAcceptHeader(request.get("browserAcceptHeader").toString());
        server.setBrowserLanguage(request.get("browserLanguage").toString());
        server.setBrowserScreenHeight(request.get("browserScreenHeight").toString());
        server.setBrowserScreenWidth(request.get("browserScreenWidth").toString());
        server.setBrowserScreenColorDepth(request.get("browserColorDepth").toString());
        server.setBrowserTimeZone(request.get("browserTZ").toString());
        server.setBrowserUserAgent(request.get("browserUserAgent").toString());
        server.setBrowserIPAddress(request.get("browserIP").toString());

        server.setCardNumber(request.get("acctNumber").toString());
        server.setCardExpDate(request.get("cardExpiryDate").toString());
        server.config("TransactionType=" + request.get("transType").toString());

        server.sendAuthRequest();
    }

    public static void initMcClientMtf(Client client) throws Exception {
        System.out.println("initMcClientMtf...");
        Certificate certificate = new Certificate(
                Certificate.cstJKSFile,
                "C:\\Users\\akylbekz\\Desktop\\3ds-test\\mc\\cnp-mtf\\3ds-mc-mtf-client.jks",
                "123456",
                "*"
                //"CN=test.processing.kz,OU=3DSS-MTF-SVR-V210-JSC-NURBANK-79054-NUR,O=Joint Stock Company \"Nurbank\",C=KZ"
        );
        client.setSSLCert(certificate);

        Certificate certDs = new Certificate(
                Certificate.cstJKSFile,
                "C:\\Users\\akylbekz\\Desktop\\3ds-test\\mc\\3ds-mc-mtf-trusted.jks",
                "123456",
                "*"
                //"CN=mtf.3ds2.directory.mastercard.com,OU=BIP01 AIDC,O=MasterCard Worldwide,C=US"
        );

        client.setSSLAcceptServerCert(certDs);
        client.setDirectoryServerCert(certificate);
    }

    public static void initMcMtf(Server server) throws Exception {
        System.out.println("initMcMtf...");
        Certificate certificate = new Certificate(
                Certificate.cstJKSFile,
                "C:\\Users\\akylbekz\\Desktop\\3ds-test\\mc\\nur-mtf\\nurbank-3ds-mc-mtf-client.jks",
                "123456",
                "*"
                //"CN=test.processing.kz,OU=3DSS-MTF-SVR-V210-JSC-NURBANK-79054-NUR,O=Joint Stock Company \"Nurbank\",C=KZ"
        );
        server.setSSLCert(certificate);

        Certificate certDs = new Certificate(
                Certificate.cstJKSFile,
                "C:\\Users\\akylbekz\\Desktop\\3ds-test\\mc\\3ds-mc-mtf-trusted.jks",
                "123456",
                "*"
                //"CN=mtf.3ds2.directory.mastercard.com,OU=BIP01 AIDC,O=MasterCard Worldwide,C=US"
        );

        server.setSSLAcceptServerCert(certDs);
    }

    public static void testVisaProd(Server server) throws Exception {
        server.setDirectoryServerURL("https://3ds2.visa.com/VbV2DS2.0/DS2/authenticate");
        server.config("ServerRefNumber=3DS_LOA_SER_NSOF_020200_00416");
        server.config("ServerOperatorId=10045985");

        initVisaProd(server);

        System.out.println(server.getSSLCert().getSubject());
        System.out.println(server.getSSLAcceptServerCert().getSubject());

        //getCardRanges(server);

        testAReq(server);
    }

    public static void testVsts(Server server) throws Exception {
        server.setDirectoryServerURL("https://VisaSecureTestSuite-vsts.3dsecure.net/ds2");
        server.config("ServerRefNumber=#3DS_LOA_SER_NSOF_020200_00416");
        server.config("ServerOperatorId=00000011");
        //server.config("ServerOperatorId=10045985");

        init(server);

        System.out.println(server.getSSLCert().getSubject());
        System.out.println(server.getSSLAcceptServerCert().getSubject());


        //getCardRanges(server);

        testAReq(server);
    }

    public static void testN(Server server) throws Exception {
        server.setDirectoryServerURL("https://3dstest.nsoftware.com/DirectoryServer.aspx");

        initN(server);
        getCardRanges(server);

        /*testAReqN(server, "7654310000000111");
        testAReqN(server, "7654310000000112");
        testAReqN(server, "7654310000000113");
        testAReqN(server, "7654310000000114");
        testAReqN(server, "7654310000000115");*/

        //testCReqN(server);
    }

    public static void getCardRanges(Server server) throws Exception {
        //server.setSerialNumber("11");
        server.requestCardRanges();

        System.out.println(server.getSerialNumber());
        System.out.println(server.getCardRanges().size());

        for (CardRangeInfo info : server.getCardRanges()) {
            System.out.println(info.getStart());
            System.out.println(info.getEnd());
            System.out.println(info.getMethodURL());
            System.out.println(info.getAction());
            System.out.println(info.getDSStartProtocolVersion());
            System.out.println(info.getDSEndProtocolVersion());
            System.out.println(info.getACSInformationIndicator());
            System.out.println(info.getACSStartProtocolVersion());
            System.out.println(info.getACSEndProtocolVersion());
        }
    }

    public static void initVisaProd(Server server) throws Exception {
        System.out.println("initVisaProd...");
        Certificate certificate = new Certificate(
                Certificate.cstJKSFile,
                "C:\\Users\\akylbekz\\Desktop\\3ds-prod\\visa3ds20_prod_nurbank_new.jks",
                "3dsecure",
                //"*"
                "CN=visa3ds.processing.kz,OU=CNP Processing GmbH,O=NURBANK JSC,C=CH,ST=ZUG,L=ZUG"
        );
        server.setSSLCert(certificate);

        /*Certificate certDs = new Certificate(
                Certificate.cstJKSFile,
                "C:\\Users\\akylbekz\\Desktop\\3ds-test\\nur\\3ds.jks",
                "123456",
                //"C=US,ST=Virginia,L=Ashburn,O=Visa Inc,OU=Operations and Infrastructure,CN=VisaSecureTestSuite-vsts.3dsecure.net"
                "C=US,O=Visa,OU=Cybersecurity,CN=Test Visa ECC Root QA"
        );

        server.setSSLAcceptServerCert(certDs);*/
    }

    public static void init(Server server) throws Exception {
        Certificate certificate = new Certificate(
                Certificate.cstJKSFile,
                "C:\\Users\\akylbekz\\Desktop\\3ds-test\\nur\\3ds.jks",
                "123456",
                //"*"
                "E=akylbek.zhumabayev@processing.kz,CN=test.processing.kz,OU=3dsserver,O=Processing,C=KZ,ST=Almaty,L=Almaty"
        );
        server.setSSLCert(certificate);

        Certificate certDs = new Certificate(
                Certificate.cstJKSFile,
                "C:\\Users\\akylbekz\\Desktop\\3ds-test\\nur\\3ds.jks",
                "123456",
                //"C=US,ST=Virginia,L=Ashburn,O=Visa Inc,OU=Operations and Infrastructure,CN=VisaSecureTestSuite-vsts.3dsecure.net"
                "C=US,O=Visa,OU=Cybersecurity,CN=Test Visa ECC Root QA"
        );

        server.setSSLAcceptServerCert(certDs);

        /*Certificate certDs = new Certificate();
        certDs.setStoreType(Certificate.cstPublicKeyFile);
        certDs.setStore( "C:\\Users\\akylbekz\\Desktop\\3ds-test\\vsts.pem");
        server.setSSLAcceptServerCert(certDs);*/
    }

    public static void initN(Server server) throws Exception {
        server.setRequestorId("01");
        server.setRequestorName("cnp");
        server.setRequestorURL("https://www.processing.kz");
        server.setMerchantCategoryCode("6012");
        server.setMerchantCountryCode("USA");
        server.setMerchantName("cnp");
        server.setAcquirerBIN("400551");
        server.setAcquirerMerchantId("555");


        server.setBrowserJavaEnabledVal(0);
        server.setBrowserJavaScriptEnabledVal(1);
        server.setBrowserAcceptHeader("test");
        server.setBrowserLanguage("ru");
        server.setBrowserScreenHeight("200");
        server.setBrowserScreenWidth("500");
        server.setBrowserScreenColorDepth("3");
        server.setBrowserTimeZone("240");
        server.setBrowserUserAgent("chrome");


        server.setMessageCategory("01");
        server.setPurchaseAmount("1000");
        server.setPurchaseDate("20210820111111");

        server.setResultsURL("https://www.processing.kz/results");
        server.setNotificationURL("https://www.processing.kz/notification");

        server.setCardNumber("7654310000000111");
        //server.setCardNumber("7654310000000112");
        server.setCardExpDate("2112");
        server.setDeviceChannel("02");
        server.setAcquirerBIN("400551");
        server.setAcquirerMerchantId("3DSTestSuite-123456789");
    }

    public static void testAReq(Server server) throws Exception {
        server.setPurchaseAmount("2");

        server.setMessageCategory("01");
        server.setRequestorId("01");
        server.setRequestorName("cnp");
        //server.setRequestorURL("https://visa-3dss.d8corporation.com:9643/api/cresponse");
        server.setRequestorURL("https://test.processing.kz/api/cresponse");
        server.setAcquirerBIN("400551");
        server.setAcquirerMerchantId("555");
        server.setMerchantCategoryCode("6012");
        server.setMerchantCountryCode("398");
        server.setMerchantName("cnp");
        server.setPurchaseDate("20210819111111");

        server.setResultsURL("https://www.processing.kz");
        server.setNotificationURL("https://www.processing.kz");

        server.setBrowserJavaEnabledVal(1);
        server.setBrowserJavaScriptEnabledVal(1);
        server.setBrowserAcceptHeader("test");
        server.setBrowserLanguage("ru");
        server.setBrowserScreenHeight("200");
        server.setBrowserScreenWidth("500");
        server.setBrowserScreenColorDepth("32");
        server.setBrowserTimeZone("-300");
        server.setBrowserUserAgent("chrome");

        //server.setCardNumber("4012000000001006"); //Y
        //server.setCardNumber("4012000000001014"); //N
        //server.setCardNumber("4012000000001022"); //U
        //server.setCardNumber("4012000000001030"); //R
        //server.setCardNumber("4012000000001048"); //I
        //server.setCardNumber("4012000000001055"); //A
        //server.setDeviceChannel("02");

        server.setCardNumber("4012000000001048");
        server.setCardExpDate("2112");
        server.config("TransactionType=01");

        //server.setDeviceChannel("03");
        //server.config("ThreeRIIndicator=06");
        server.config("RequestorChallengeInd=06");

        server.sendAuthRequest();
    }

    public static void testAReqN(Server server, String pan) throws Exception {
        server.setCardNumber(pan);
        server.setCardExpDate("2112");
        server.setDeviceChannel("02");
        server.setAcquirerBIN("400551");
        server.setAcquirerMerchantId("3DSTestSuite-123456789");

        //server.setMethodNotificationURL("U");

        server.sendAuthRequest();

        System.out.println(pan);
        System.out.println(server.getTransactionStatus());
        System.out.println(server.getAuthenticationECI());
        System.out.println(server.getAuthenticationValue());
    }

    public static void testCReqN(Server server) throws Exception {
        testAReqN(server, "7654370980201119");

        if (server.getTransactionStatus().equals("C")) {
            System.out.println("Challenge flow!");
            String creq = server.getChallengeRequest();
            server.config("SessionData=" + "rrn=123456789012");
            String encodedSessionData = server.config("EncodedSessionData");
            String acsUrl = server.getACSURL();

            System.out.println("<form name='downloadForm' action='" + acsUrl + "' method='POST'>");
            System.out.println("  <INPUT type='hidden' name='creq'                value='" + creq + "'>");
            System.out.println("  <input type='hidden' name='threeDSSessionData'  value='" + encodedSessionData + "'>");
            System.out.println("</form>");
            System.out.println("<script>");
            System.out.println("window.onload = submitForm;");
            System.out.println("function submitForm() { downloadForm.submit(); }");
            System.out.println("</script>");
        }
    }

    private static void initListener(Server server) {
        try {
            server.addServerEventListener(new ServerEventListener() {
                @Override
                public void cardRange(ServerCardRangeEvent serverCardRangeEvent) {

                }

                @Override
                public void dataPacketIn(ServerDataPacketInEvent serverDataPacketInEvent) {

                }

                @Override
                public void dataPacketOut(ServerDataPacketOutEvent serverDataPacketOutEvent) {

                }

                @Override
                public void error(ServerErrorEvent serverErrorEvent) {

                }

                @Override
                public void log(ServerLogEvent serverLogEvent) {

                }

                @Override
                public void messageExtension(ServerMessageExtensionEvent serverMessageExtensionEvent) {

                }

                @Override
                public void SSLServerAuthentication(ServerSSLServerAuthenticationEvent serverSSLServerAuthenticationEvent) {
                    serverSSLServerAuthenticationEvent.accept = true;
                }

                @Override
                public void SSLStatus(ServerSSLStatusEvent serverSSLStatusEvent) {
                    //System.out.println(serverSSLStatusEvent.message);
                }
            });
        } catch (Exception ex) {
            System.out.println(ex.getMessage());;
        }
    }

}
