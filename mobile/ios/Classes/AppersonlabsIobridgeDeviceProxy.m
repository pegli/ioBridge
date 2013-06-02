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

#define kAPIKeyHeaderName @"X-APIKEY"

#define kConnectionStateResource @"gateway/request/state"

typedef void (^JsonSuccess)(NSURLRequest *request, NSHTTPURLResponse *response, id JSON);
typedef void (^JsonFailure)(NSURLRequest *request, NSHTTPURLResponse *response, NSError *error, id JSON);


@interface AppersonlabsIobridgeDeviceProxy ()
@property (nonatomic, strong) AFHTTPClient * client;
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
    
    NSDictionary * params = [NSDictionary dictionaryWithObjectsAndKeys:self.apikey, @"apikey", self.serial, @"serial", nil];
    NSMutableURLRequest * req = [self.client requestWithMethod:@"GET" path:kConnectionStateResource parameters:params];
    
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

- (void)readGPIORegister:(id)args {
    
}

- (void)sendData:(id)args {
    
}

- (void)writeGPIORegister:(id)args {
    
}

// TODO stream events

@end
