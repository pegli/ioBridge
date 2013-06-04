//
//  StreamingJSONRequestOperation.h
//  iobridge
//
//  Created by Paul Mietz Egli on 6/3/13.
//
//

#import "AFJSONRequestOperation.h"

@interface StreamingJSONRequestOperation : AFJSONRequestOperation

+ (instancetype)JSONRequestOperationWithRequest:(NSURLRequest *)urlRequest receivedJSONBlock:(id)blk;

@end
