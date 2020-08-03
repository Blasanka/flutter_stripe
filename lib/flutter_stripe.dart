import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class FlutterStripe {
  static const MethodChannel _channel = const MethodChannel('flutterstripe');

  static Future<String> init(String publishableKey) async {
    Map<String, dynamic> arg = {"publishableKey": publishableKey};
    String message = await _channel.invokeMethod('initializeStripe', arg);
    return message;
  }

  static Future<String> confirmPayment({@required String token,
      @required String clientSecret}) async {
    Map<String, String> args = {
      "token": token,
      "clientSecret": clientSecret,
    };
    String message = await _channel.invokeMethod('confirmPayment', args);
    return message;
  }
}

