//
//  DeviceProxy.h
//  iobridge
//
//  Created by Paul Mietz Egli on 6/2/13.
//
//

#import "TiProxy.h"

@interface AppersonlabsIobridgeDeviceProxy : TiProxy

@property (nonatomic, readonly) NSString * apikey;
@property (nonatomic, readonly) NSString * serial;

@end
