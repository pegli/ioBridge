//
//  StreamingJSONRequestOperation.m
//  iobridge
//
//  Created by Paul Mietz Egli on 6/3/13.
//
//

#import "StreamingJSONRequestOperation.h"
#import "TiBase.h"
#import "YAJLParser.h"

@interface StreamingJSONRequestOperation () <YAJLParserDelegate, NSStreamDelegate>
@property (nonatomic, strong) YAJLParser * parser;
@property (nonatomic, strong) NSInputStream * pipedInputStream;
@property (nonatomic, strong) ReceivedJSONBlock receivedJSONBlock;

@property (nonatomic, strong) NSMutableDictionary * streamedDictionary;
@property (nonatomic, strong) NSString * lastKey;

@end

@implementation StreamingJSONRequestOperation

+ (instancetype)JSONRequestOperationWithRequest:(NSURLRequest *)urlRequest receivedJSONBlock:(ReceivedJSONBlock)blk {
    StreamingJSONRequestOperation * requestOperation = [(StreamingJSONRequestOperation *)[self alloc] initWithRequest:urlRequest];
    requestOperation.receivedJSONBlock = blk;
    return requestOperation;
}

- (instancetype)initWithRequest:(NSURLRequest *)urlRequest {
    if (self = [super initWithRequest:urlRequest]) {
        TiThreadPerformOnMainThread(^{
            CFReadStreamRef     readStream;
            CFWriteStreamRef    writeStream;
            
            CFStreamCreateBoundPair(NULL, &readStream, &writeStream, 4096);
            
            NSInputStream * pipedInputStream = CFBridgingRelease(readStream);
            [pipedInputStream scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSRunLoopCommonModes];
            pipedInputStream.delegate = self;
            [pipedInputStream open];
            self.pipedInputStream = pipedInputStream;
            
            self.outputStream = CFBridgingRelease(writeStream);
        }, YES);
    }
    return self;
}

#pragma mark NSStreamDelegate

- (void)stream:(NSStream *)stream handleEvent:(NSStreamEvent)eventCode {
    switch (eventCode) {
        case NSStreamEventHasBytesAvailable: {
            // read stream into NSData object
            uint8_t buf[1024];
            unsigned int len = 0;
            len = [(NSInputStream *)stream read:buf maxLength:1024];
            if (!len) {
                break;
            }

            // iterate over JSON object in the NSData object
            unsigned int start = 0;
            YAJLParserStatus status = YAJLParserStatusNone;
            do {
                // you can't reset a parser, so a new one needs to be created each time
                // parsing completes successfully.
                if (!self.parser) {
                    self.parser = [[YAJLParser alloc] initWithParserOptions:YAJLParserOptionsAllowComments];
                    self.parser.delegate = self;
                }
            
                status = [self.parser parse:[NSData dataWithBytes:&buf[start] length:len-start]];
                start += self.parser.bytesConsumed;
                
                if (status = YAJLParserStatusOK) {
                    self.parser = nil;
                }
            } while (status == YAJLParserStatusOK && start < len);

        }
        default:
            break;
    }
}

#pragma mark YAJLParserDelegate

- (void)parserDidStartDictionary:(YAJLParser *)parser {
    self.streamedDictionary = [NSMutableDictionary dictionary];
}

- (void)parserDidEndDictionary:(YAJLParser *)parser {
    TiThreadPerformOnMainThread(^{
        self.receivedJSONBlock(self.streamedDictionary, parser.parserError);
    }, NO);
}

- (void)parserDidStartArray:(YAJLParser *)parser {
}

- (void)parserDidEndArray:(YAJLParser *)parser {
}

- (void)parser:(YAJLParser *)parser didMapKey:(NSString *)key {
    self.lastKey = key;
}

- (void)parser:(YAJLParser *)parser didAdd:(id)value {
    [self.streamedDictionary setObject:value forKey:self.lastKey];
}


@end
