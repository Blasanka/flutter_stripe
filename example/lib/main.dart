import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_stripe/flutter_stripe.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _status = 'Unknown';

  @override
  void initState() {
    super.initState();
    initStripe();
  }

  Future<void> initStripe() async {
    String status;
    try {
      status = await FlutterStripe.init(
          "pk_test_7jU29EQEzQEE8LQeWfGZWmiH00nti0ceqA");
    } on PlatformException {
      status = 'Failed to initialize Stripe.';
    } catch (e) {
      status = 'Failed ${e.getMessage()}';
    }

    if (!mounted) return;

    setState(() {
      _status = status;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Running on: $_status\n'),
        ),
        floatingActionButton: Builder(builder: (BuildContext context) {
          return FloatingActionButton(
            onPressed: () async {
              showDialog(
                  context: context,
                  builder: (_) => Dialog(
                      child: Center(child: CircularProgressIndicator())));
              String message = '';
              try {
                message = await FlutterStripe.confirmPayment(
                    token: "pm_1HAw1bFVAa7A6hgczTdYmtgx",
                    clientSecret:
                    "seti_1HBwe3FVAa7A6hgcQMUfPDAI_secret_HlTbhfnDOw7OMtyCVEOzg6USIYPpMmB");
              } on PlatformException {
                message = 'Failed to initialize Stripe.';
              } catch (e) {
                message = 'Failed ${e.getMessage()}';
              }
              setState(() {
                _status = message;
              });
              Navigator.pop(context);
            },
            child: Icon(Icons.add),
          );
        }),
      ),
    );
  }
}
