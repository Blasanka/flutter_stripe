import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_stripe/flutter_stripe.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_stripe');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('FlutterStripe.init', () async {
    expect(await FlutterStripe.init("publishableKey"), isNull);
  });
}
