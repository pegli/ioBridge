//
//  StreamingJSONRequestOperation.m
//  iobridge
//
//  Created by Paul Mietz Egli on 6/3/13.
//
//

#import "StreamingJSONRequestOperation.h"

@implementation StreamingJSONRequestOperation

+ (instancetype)JSONRequestOperationWithRequest:(NSURLRequest *)urlRequest receivedJSONBlock:(id)blk {
    StreamingJSONRequestOperation *requestOperation = [(StreamingJSONRequestOperation *)[self alloc] initWithRequest:urlRequest];

    // TODO hook up output stream
    
    return requestOperation;
}

@end
