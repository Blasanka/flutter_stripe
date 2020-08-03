package com.blasanka.flutter_stripe;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;

import java.lang.ref.WeakReference;
import java.util.Objects;

import io.flutter.app.FlutterActivity;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

public class FlutterStripePlugin extends FlutterActivity implements FlutterPlugin, MethodCallHandler {
    private Context context;
    private MethodChannel channel;

  private Result flutterResult;
  private Stripe stripe;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutterstripe");
    channel.setMethodCallHandler(this);
    context = flutterPluginBinding.getApplicationContext();
  }

  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutterstripe");
    channel.setMethodCallHandler(new FlutterStripePlugin());
//    context = registrar.activity().getApplication();
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    flutterResult = result;
    if (call.method.equals("initializeStripe")) {
      String stripePk = call.argument("publishableKey");
      initStripe(stripePk);
    } else if (call.method.equals("confirmPayment")) {
      String token = call.argument("token");
      String clientSecret = call.argument("clientSecret");
      confirmPayment(token, clientSecret);
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(this));
  }

  void initStripe(String stripePk) {
      try {
          stripe = new Stripe(
                  context,
                  Objects.requireNonNull(stripePk)
          );
          flutterResult.success("Stripe initialized" + stripePk);
      } catch(Exception e) {
          flutterResult.error("Error occurred",
                  "Cannot initialize stripe", e.getMessage());
      }
  }

  void confirmPayment(String token, String clientSecret) {
      try {
          if (token != null && clientSecret != null) {
              ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                      .createWithPaymentMethodId(token, clientSecret);
              stripe.confirmPayment(this, confirmParams);
          } else {
              flutterResult.error("Failed",
                      "Token or clientSecret is null",
                      "token: " + token + " clientSecret: " + clientSecret);
          }
      } catch(Exception e) {
        flutterResult.error("Error occurred",
                "Cannot confirmPayment", e.getMessage());
    }
  }

  private static final class PaymentResultCallback
          implements ApiResultCallback<PaymentIntentResult> {

    @NonNull private final WeakReference<FlutterStripePlugin> activityRef;
    PaymentResultCallback(@NonNull FlutterStripePlugin activity) {
      activityRef = new WeakReference<>(activity);
    }

    @Override
    public void onSuccess(@NonNull PaymentIntentResult result) {
      final FlutterStripePlugin activity = activityRef.get();
      if (activity == null) {
        return;
      }

      PaymentIntent paymentIntent = result.getIntent();
      PaymentIntent.Status status = paymentIntent.getStatus();

      if (status == PaymentIntent.Status.Succeeded) {
        // Payment completed successfully

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        activity.displayAlert(
                "Payment completed",
                gson.toJson(paymentIntent)
        );
        activity.flutterResult.success("Succeeded");
      } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
        String errorDetails = Objects.requireNonNull(paymentIntent.getLastPaymentError()).getMessage();
        activity.displayAlert(
                "Payment failed",
                errorDetails
        );
        activity.flutterResult.error("Failed", "payment confirmation failed", errorDetails);
      }
    }

    @Override
    public void onError(@NonNull Exception e) {
      final FlutterStripePlugin activity = activityRef.get();
      if (activity == null) {
        return;
      }
      // Payment request failed – allow retrying using the same payment method
      activity.displayAlert("Error", e.toString());
      activity.flutterResult.error("Error", "Error occur while stripe payment", e.toString());
    }
  }

//  private void onPaymentSuccess(@NonNull final Response response) throws IOException {
//    Gson gson = new Gson();
//    Type type = new TypeToken<Map<String, String>>(){}.getType();
//    Map<String, String> responseMap = gson.fromJson(
//            Objects.requireNonNull(response.body()).string(),
//            type
//    );
//    paymentIntentClientSecret = responseMap.get("clientSecret");
//  }

  private void displayAlert(@NonNull String title, @Nullable String message) {

    AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message);
    builder.setPositiveButton("Ok", null);
    builder.create().show();
  }
}
