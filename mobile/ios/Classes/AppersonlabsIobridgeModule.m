/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "AppersonlabsIobridgeModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"

@implementation AppersonlabsIobridgeModule

#pragma mark Internal

-(id)moduleGUID {
	return @"569cc569-8ccc-4ac5-a498-ad0451b6c0c7";
}

-(NSString*)moduleId {
	return @"appersonlabs.iobridge";
}

#pragma mark Lifecycle

-(void)startup {
	[super startup];
	
	NSLog(@"[INFO] %@ loaded",self);
}

-(void)shutdown:(id)sender {
	[super shutdown:sender];
}

+ (NSDictionary *)errorAsDict:(NSError *)err {
    return [NSDictionary dictionaryWithObjectsAndKeys:
            NUMINT(err.code), @"code",
            err.description, @"description",
            nil];
}

#pragma mark API

/*
- (id)getDevice:(id)args {
    NSString * apikey;
    NSString * serial;
    ENSURE_ARG_AT_INDEX(apikey, args, 0, NSString)
    ENSURE_ARG_AT_INDEX(serial, args, 1, NSString);
    
    return [[DeviceProxy alloc] initWith]
}
 */

@end
