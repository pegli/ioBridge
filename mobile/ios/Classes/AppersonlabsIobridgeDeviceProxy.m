//
//  DeviceProxy.m
//  iobridge
//
//  Created by Paul Mietz Egli on 6/2/13.
//
//

#import "AppersonlabsIobridgeDeviceProxy.h"
#import "AppersonlabsIobridgeModule.h"

#import "AFHTTPClient.h"
#import "AFJSONRequestOperation.h"

#import "StreamingJSONRequestOperation.h"

#define kAPIKeyHeaderName @"X-APIKEY"

#define kConnectionStateResource @"gateway/request/state"
#define kReadGPIORegisterResource @"gateway/request/register/read"
#define kWriteGPIORegisterResource @"gateway/request/register/write"
#define kSendDataResource @"gateway/send"


typedef void (^JsonSuccess)(NSURLRequest *request, NSHTTPURLResponse *response, id JSON);
typedef void (^JsonFailure)(NSURLRequest *request, NSHTTPURLResponse *response, NSError *error, id JSON);

@interface AppersonlabsIobridgeDeviceProxy ()
@property (nonatomic, strong) AFHTTPClient * client;
- (void)sendGETJSONRequestWithPath:(NSString *)path parameters:(NSDictionary *)params callback:(KrollCallback *)callback;
- (void)sendPOSTJSONRequestWithPath:(NSString *)path parameters:(NSDictionary *)params callback:(KrollCallback *)callback;
@end

@implementation AppersonlabsIobridgeDeviceProxy

- (void)_initWithProperties:(NSDictionary *)properties {
    [super _initWithProperties:properties];
    
    // TODO make base URL configurable?
    self.client = [AFHTTPClient clientWithBaseURL:[NSURL URLWithString:@"http://api.realtime.io/v1"]];
    [self.client setDefaultHeader:kAPIKeyHeaderName value:self.apikey];
}

- (void)setApikey:(NSString *)apikey {
    if (!self.apikey) {
        _apikey = apikey;
    }
}

- (void)setSerial:(NSString *)serial {
    if (!self.serial) {
        _serial = serial;
    }
}

- (void)fetchConnectionState:(id)args {
    KrollCallback * callback;
    ENSURE_ARG_OR_NIL_AT_INDEX(callback, args, 0, KrollCallback);
    
    NSDictionary * params = [NSDictionary dictionaryWithObjectsAndKeys:
                             self.apikey, @"apikey",
                             self.serial, @"serial",
                             nil];
    [self sendGETJSONRequestWithPath:kConnectionStateResource parameters:params callback:callback];
}

- (void)readGPIORegister:(id)args {
    NSNumber * channel;
    NSString * registerName;
    KrollCallback * callback;
    ENSURE_ARG_AT_INDEX(channel, args, 0, NSNumber)
    ENSURE_ARG_AT_INDEX(registerName, args, 1, NSString)
    ENSURE_ARG_OR_NIL_AT_INDEX(callback, args, 2, KrollCallback);
    
    NSDictionary * params = [NSDictionary dictionaryWithObjectsAndKeys:
                             channel, @"channel",
                             self.serial, @"serial",
                             registerName, @"register",
                             nil];
    [self sendPOSTJSONRequestWithPath:kReadGPIORegisterResource parameters:params callback:callback];
}

- (void)sendData:(id)args {
    NSNumber * channel;
    NSString * payload;
    NSString * encoding;
    KrollCallback * callback;
    ENSURE_ARG_AT_INDEX(channel, args, 0, NSNumber)
    ENSURE_ARG_AT_INDEX(payload, args, 1, NSString)
    ENSURE_ARG_OR_NIL_AT_INDEX(encoding, args, 2, NSString)
    ENSURE_ARG_OR_NIL_AT_INDEX(callback, args, 3, KrollCallback)
    
    NSDictionary * params = [NSDictionary dictionaryWithObjectsAndKeys:
                             channel, @"channel",
                             self.serial, @"serial",
                             (encoding ? encoding : @"plain"), @"encoding",
                             payload, @"payload",
                             nil];
    [self sendPOSTJSONRequestWithPath:kSendDataResource parameters:params callback:callback];
    
}

- (void)writeGPIORegister:(id)args {
    NSNumber * channel;
    NSString * registerName;
    NSString * content;
    KrollCallback * callback;
    ENSURE_ARG_AT_INDEX(channel, args, 0, NSNumber)
    ENSURE_ARG_AT_INDEX(registerName, args, 1, NSString)
    ENSURE_ARG_AT_INDEX(content, args, 2, NSString)
    ENSURE_ARG_OR_NIL_AT_INDEX(callback, args, 3, KrollCallback);
    
    NSDictionary * params = [NSDictionary dictionaryWithObjectsAndKeys:
                             channel, @"channel",
                             self.serial, @"serial",
                             registerName, @"register",
                             content, @"content",
                             nil];
    [self sendPOSTJSONRequestWithPath:kWriteGPIORegisterResource parameters:params callback:callback];
}

#pragma mark Stream Events

-(void)_listenerAdded:(NSString*)type count:(int)count {
    if (count == 1 && [type isEqualToString:@"stream"]) {
        // TODO start long-lived stream connection
        NSDictionary * params = [NSDictionary dictionaryWithObject:self.apikey forKey:@"apikey"];
        NSMutableURLRequest * req = [self.client requestWithMethod:@"GET" path:@"stream" parameters:params];
        
        StreamingJSONRequestOperation * op = [StreamingJSONRequestOperation JSONRequestOperationWithRequest:req receivedJSONBlock:^(id obj, NSError * error) {
            if ([[obj class] isSubclassOfClass:[NSDictionary class]]) {
                [self fireEvent:@"stream" withObject:obj];
            }
        }];
        
        [self.client enqueueHTTPRequestOperation:op];
        NSLog(@"started stream listener");
    }
}

-(void)_listenerRemoved:(NSString*)type count:(int)count {
    if (count < 1) {
        [self.client cancelAllHTTPOperationsWithMethod:@"GET" path:@"stream"];
        NSLog(@"stopped stream listener");
    }
}

#pragma mark JSON Request Utilities

// TODO maybe move into subclass of AFHTTPClient?

- (void)sendGETJSONRequestWithPath:(NSString *)path parameters:(NSDictionary *)params callback:(KrollCallback *)callback {
    NSMutableURLRequest * req = [self.client requestWithMethod:@"GET" path:path parameters:params];
    
    JsonSuccess success = ^(NSURLRequest * request, NSHTTPURLResponse * response, id JSON) {
        TiThreadPerformOnMainThread(^{
            [callback call:[NSArray arrayWithObjects:[NSNull null], JSON, nil] thisObject:nil];
        }, NO);
    };
    
    JsonFailure failure = ^(NSURLRequest *request, NSHTTPURLResponse *response, NSError *error, id JSON) {
        TiThreadPerformOnMainThread(^{
            [callback call:[NSArray arrayWithObjects:[AppersonlabsIobridgeModule errorAsDict:error], JSON, nil] thisObject:nil];
        }, NO);
    };
    
    AFJSONRequestOperation * op = [AFJSONRequestOperation JSONRequestOperationWithRequest:req success:success failure:failure];
    [self.client enqueueHTTPRequestOperation:op];
}

- (void)sendPOSTJSONRequestWithPath:(NSString *)path parameters:(NSDictionary *)params callback:(KrollCallback *)callback {
    NSMutableURLRequest * req = [self.client requestWithMethod:@"POST" path:path parameters:params];
    [req setHTTPBody:[NSJSONSerialization dataWithJSONObject:params options:nil error:nil]];
    [req addValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    
    JsonSuccess success = ^(NSURLRequest * request, NSHTTPURLResponse * response, id JSON) {
        TiThreadPerformOnMainThread(^{
            [callback call:[NSArray arrayWithObjects:[NSNull null], JSON, nil] thisObject:nil];
        }, NO);
    };
    
    JsonFailure failure = ^(NSURLRequest *request, NSHTTPURLResponse *response, NSError *error, id JSON) {
        TiThreadPerformOnMainThread(^{
            [callback call:[NSArray arrayWithObjects:[AppersonlabsIobridgeModule errorAsDict:error], JSON, nil] thisObject:nil];
        }, NO);
    };
    
    AFJSONRequestOperation * op = [AFJSONRequestOperation JSONRequestOperationWithRequest:req success:success failure:failure];
    [self.client enqueueHTTPRequestOperation:op];
}

@end
