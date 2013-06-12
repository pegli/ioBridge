//
//  StreamingJSONRequestOperation.h
//  iobridge
//
//  Created by Paul Mietz Egli on 6/3/13.
//
//

#import "AFJSONRequestOperation.h"

typedef void (^ReceivedJSONBlock)(id object, NSError * error);

@interface StreamingJSONRequestOperation : AFJSONRequestOperation

+ (instancetype)JSONRequestOperationWithRequest:(NSURLRequest *)urlRequest receivedJSONBlock:(ReceivedJSONBlock)blk;

@end
