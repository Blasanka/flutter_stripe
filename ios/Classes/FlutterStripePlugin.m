#import "FlutterStripePlugin.h"
#if __has_include(<flutter_stripe/flutter_stripe-Swift.h>)
#import <flutter_stripe/flutter_stripe-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_stripe-Swift.h"
#endif

@implementation FlutterStripePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterStripePlugin registerWithRegistrar:registrar];
}
@end
