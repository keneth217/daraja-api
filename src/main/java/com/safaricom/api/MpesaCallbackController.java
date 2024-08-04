package com.safaricom.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MpesaCallbackController {

    private static final Logger logger = LoggerFactory.getLogger(MpesaCallbackController.class);

    @PostMapping("/stkpush")
    public ResponseEntity<String> handleCallback(@RequestBody MpesaCallbackResponse response) {
        MpesaCallbackResponse.Body body = response.getBody();
        MpesaCallbackResponse.StkCallback stkCallback = body.getStkCallback();

        logger.info("Received M-PESA callback:");
        logger.info("MerchantRequestID: {}", stkCallback.getMerchantRequestID());
        logger.info("CheckoutRequestID: {}", stkCallback.getCheckoutRequestID());
        logger.info("ResultCode: {}", stkCallback.getResultCode());
        logger.info("ResultDesc: {}", stkCallback.getResultDesc());

        switch (stkCallback.getResultCode()) {
            case 0: // Success
                List<MpesaCallbackResponse.Item> items = stkCallback.getCallbackMetadata().getItem();
                double amount = 0.0;
                String mpesaReceiptNumber = "";
                long transactionDate = 0;
                long phoneNumber = 0;

                for (MpesaCallbackResponse.Item item : items) {
                    switch (item.getName()) {
                        case "Amount":
                            if (item.getValue() instanceof Number) {
                                amount = ((Number) item.getValue()).doubleValue();
                            }
                            break;
                        case "MpesaReceiptNumber":
                            mpesaReceiptNumber = item.getValue().toString();
                            break;
                        case "TransactionDate":
                            transactionDate = Long.parseLong(item.getValue().toString());
                            break;
                        case "PhoneNumber":
                            phoneNumber = Long.parseLong(item.getValue().toString());
                            break;
                    }
                }

                logger.info("Payment Details:");
                logger.info("Amount: {}", amount);
                logger.info("MpesaReceiptNumber: {}", mpesaReceiptNumber);
                logger.info("TransactionDate: {}", transactionDate);
                logger.info("PhoneNumber: {}", phoneNumber);

                // TODO: Save payment details to your database and update payment status

                return new ResponseEntity<>("Callback handled successfully", HttpStatus.OK);

            case 1025:
                // An error occurred while sending a push request
                logger.error("Error 1025: An error occurred while sending a push request.");
                logger.error("Possible causes: System error or USSD message too long.");
                logger.error("Solution: Retry the request. Ensure the messaging is less than 182 characters.");
                return new ResponseEntity<>("Error occurred while sending push request", HttpStatus.BAD_REQUEST);

            case 1037:
                // No response from the user
                logger.error("Error 1037: No response from the user.");
                logger.error("Possible cause: Backend API issue, not user-related.");
                logger.error("Solution: Retry the request after receiving the callback and notify the user.");
                return new ResponseEntity<>("No response from the user", HttpStatus.BAD_REQUEST);

            case 1032:
                // The request was canceled by the user
                logger.warn("Error 1032: The request was canceled by the user.");
                logger.warn("Possible causes: STK prompt timed out or user canceled the request.");
                logger.warn("Solution: Inform the user or cancel the transaction and try again.");
                return new ResponseEntity<>("Request canceled by the user", HttpStatus.OK);

            case 1:
                // Insufficient balance
                logger.error("Error 1: The balance is insufficient for the transaction.");
                logger.error("Possible cause: User has declined using Fuliza or has insufficient funds.");
                logger.error("Solution: Advise user to deposit funds or use Fuliza.");
                return new ResponseEntity<>("Insufficient balance for the transaction", HttpStatus.BAD_REQUEST);

            case 2001:
                // Invalid initiator information
                logger.error("Error 2001: The initiator information is invalid.");
                logger.error("Possible causes: Incorrect password or M-PESA pin.");
                logger.error("Solution: Verify user credentials and ensure correct M-PESA pin.");
                return new ResponseEntity<>("Invalid initiator information", HttpStatus.BAD_REQUEST);

            case 1019:
                // Transaction has expired
                logger.error("Error 1019: Transaction has expired.");
                logger.error("Possible cause: Transaction took too long to process.");
                logger.error("Solution: Retry the transaction.");
                return new ResponseEntity<>("Transaction has expired", HttpStatus.BAD_REQUEST);

            case 1001:
                // Unable to lock subscriber
                logger.error("Error 1001: Unable to lock subscriber. A transaction is already in process.");
                logger.error("Possible causes: Duplicated MSISDN or ongoing USSD session.");
                logger.error("Solution: Close existing session or retry after 2-3 minutes.");
                return new ResponseEntity<>("Unable to lock subscriber, transaction already in process", HttpStatus.BAD_REQUEST);

            default:
                logger.error("Unknown ResultCode: {} and ResultDesc: {}", stkCallback.getResultCode(), stkCallback.getResultDesc());
                return new ResponseEntity<>("Unknown error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/confirmation")
    public ResponseEntity<String> handleConfirmation(@RequestBody String request) {
        logger.info("Received M-PESA confirmation request: {}", request);

        // Parse and handle the confirmation request here
        // This is typically where you'd update your database with the payment status

        // Example response for success
        return new ResponseEntity<>("Confirmation handled successfully", HttpStatus.OK);
    }

    @PostMapping("/validation")
    public ResponseEntity<String> handleValidation(@RequestBody String request) {
        logger.info("Received M-PESA validation request: {}", request);

        // Parse and handle the validation request here
        // This is typically where you'd validate the request before confirming it

        // Example response for success
        return new ResponseEntity<>("Validation handled successfully", HttpStatus.OK);
    }
}
