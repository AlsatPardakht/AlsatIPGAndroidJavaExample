package com.alsatpardakht.alsatipgandroidjavaexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alsatpardakht.alsatipgandroid.AlsatIPG;
import com.alsatpardakht.alsatipgandroid.data.remote.model.PaymentSignRequest;
import com.alsatpardakht.alsatipgandroid.domain.model.PaymentSignResult;
import com.alsatpardakht.alsatipgandroid.domain.model.PaymentValidationResult;

public class MainActivity extends AppCompatActivity {

    private final String API = "ENTER YOUR API KEY HERE";

    private final AlsatIPG alsatIPG = AlsatIPG.getInstance();

    private TextView logTextView;
    private Button signPaymentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logTextView = findViewById(R.id.logTextView);
        signPaymentButton = findViewById(R.id.signPaymentButton);

        signPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signPaymentButtonOnClick();
            }
        });

        observeToPaymentSignStatus();
        observeToPaymentValidationStatus();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            Uri data = intent.getData();
            if (data != null) {
                log("intent and data is not null");
                alsatIPG.validation(API, data);
            } else {
                log("data is null");
            }
        } else {
            log("intent is null");
        }
    }

    public void observeToPaymentSignStatus() {
        alsatIPG.getPaymentSignStatus().observe(this, new Observer<PaymentSignResult>() {
            @Override
            public void onChanged(PaymentSignResult paymentSignResult) {
                if (paymentSignResult.isSuccessful()) {
                    log("payment Sign Success url = " + paymentSignResult.getUrl());
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentSignResult.getUrl()));
                    startActivity(intent);
                } else if (paymentSignResult.isLoading()) {
                    log("payment Sign Loading ...");
                } else if (paymentSignResult.getError() != null) {
                    log("payment Sign error = " + paymentSignResult.getError().getMessage());
                }
            }
        });
    }

    public void observeToPaymentValidationStatus() {
        alsatIPG.getPaymentValidationStatus().observe(this, new Observer<PaymentValidationResult>() {
            @Override
            public void onChanged(PaymentValidationResult paymentValidationResult) {
                if (paymentValidationResult.isSuccessful()) {
                    log("payment Validation Success data = " + paymentValidationResult.getData());
                    if (
                            (paymentValidationResult.getData() != null) &&
                                    (paymentValidationResult.getData().getPSP().getIsSuccess()) &&
                                    (paymentValidationResult.getData().getVERIFY().getIsSuccess())
                    ) {
                        log("money transferred");
                    } else {
                        log("money has not been transferred");
                    }
                } else if (paymentValidationResult.isLoading()) {
                    log("payment Validation Loading ...");
                } else if (paymentValidationResult.getError() != null) {
                    log("payment Validation error = " + paymentValidationResult.getError().getMessage());
                }
            }
        });
    }

    private void signPaymentButtonOnClick() {
        PaymentSignRequest paymentSignRequest = new PaymentSignRequest(
                API,//Api
                "10000",//Amount
                "12345",//InvoiceNumber
                "http://www.example.com/some_path"//RedirectAddress
        );
        alsatIPG.sign(paymentSignRequest);
    }

    private void log(String message) {
        logTextView.setText(logTextView.getText() + message + "\n");
    }
}