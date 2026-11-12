// VTUAN iOS license gate, diagnostic build. No new UIWindow and no private UDID API.
// Compile using Xcode iPhoneOS SDK. It does not open or modify any game menu.
#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import <Security/Security.h>

static NSString *const kServer = @"https://vtuan-103-159-51-231.sslip.io";
static NSString *const kService = @"vn.vtuan.keygate.v1";
static NSString *const kDevice = @"device-id";
static NSString *const kKey = @"license-key";

static NSString *vtRead(NSString *account) {
    NSDictionary *q = @{(id)kSecClass:(id)kSecClassGenericPassword,
                        (id)kSecAttrService:kService,
                        (id)kSecAttrAccount:account,
                        (id)kSecReturnData:@YES,
                        (id)kSecMatchLimit:(id)kSecMatchLimitOne};
    CFTypeRef value = NULL;
    if (SecItemCopyMatching((__bridge CFDictionaryRef)q, &value) != errSecSuccess) return nil;
    return [[NSString alloc] initWithData:CFBridgingRelease(value) encoding:NSUTF8StringEncoding];
}

static BOOL vtWrite(NSString *account, NSString *value) {
    NSDictionary *q = @{(id)kSecClass:(id)kSecClassGenericPassword,
                        (id)kSecAttrService:kService,
                        (id)kSecAttrAccount:account};
    SecItemDelete((__bridge CFDictionaryRef)q);
    NSMutableDictionary *add = [q mutableCopy];
    add[(id)kSecValueData] = [value dataUsingEncoding:NSUTF8StringEncoding];
    add[(id)kSecAttrAccessible] = (id)kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly;
    return SecItemAdd((__bridge CFDictionaryRef)add, NULL) == errSecSuccess;
}

static NSString *vtDeviceID(void) {
    NSString *existing = vtRead(kDevice);
    if (existing.length) return existing;
    // IDFV is not the hardware UDID; store an app-scoped ID for repeat launches.
    NSString *idfv = [UIDevice currentDevice].identifierForVendor.UUIDString;
    NSString *device = [@"VTUAN-" stringByAppendingString:idfv.length ? idfv : [NSUUID UUID].UUIDString];
    return vtWrite(kDevice, device) ? device : nil;
}

typedef void (^VTReply)(BOOL, NSDictionary *, NSString *);
static void vtPost(NSString *path, NSString *key, NSString *device, VTReply completion) {
    NSURL *url = [NSURL URLWithString:[kServer stringByAppendingString:path]];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
    request.HTTPMethod = @"POST";
    request.timeoutInterval = 18.0;
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    NSError *err = nil;
    NSData *body = [NSJSONSerialization dataWithJSONObject:@{@"key":key, @"device_id":device}
                                                   options:0 error:&err];
    if (err || !body) {
        completion(NO, nil, @"Không tạo được JSON.");
        return;
    }
    request.HTTPBody = body;
    [[[NSURLSession sharedSession] dataTaskWithRequest:request
        completionHandler:^(NSData *data, NSURLResponse *response, NSError *networkError) {
            NSDictionary *json = nil;
            if (data.length > 0) {
                id obj = [NSJSONSerialization JSONObjectWithData:data options:0 error:NULL];
                if ([obj isKindOfClass:[NSDictionary class]]) json = (NSDictionary *)obj;
            }
            NSInteger status = [(NSHTTPURLResponse *)response statusCode];
            BOOL ok = !networkError && status == 200 && [json[@"ok"] boolValue];
            NSString *message = networkError.localizedDescription;
            if (!message) message = [json[@"message"] isKindOfClass:[NSString class]] ? json[@"message"] : nil;
            if (!message) message = [NSString stringWithFormat:@"HTTP %ld", (long)status];
            NSLog(@"[VTUAN_KEYGATE] %@ http=%ld ok=%d", path, (long)status, ok);
            dispatch_async(dispatch_get_main_queue(), ^{ completion(ok, json, message); });
    }] resume];
}

@interface VTKeyViewController : UIViewController <UITextFieldDelegate>
@property(nonatomic, copy) NSString *device;
@property(nonatomic, strong) UITextField *keyField;
@property(nonatomic, strong) UILabel *statusLabel;
@property(nonatomic, strong) UIButton *submitButton;
@property(nonatomic, assign) BOOL busy;
@end

@implementation VTKeyViewController
- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.7];

    UIView *panel = [UIView new];
    panel.backgroundColor = [UIColor colorWithWhite:0.12 alpha:0.99];
    panel.layer.cornerRadius = 14;
    panel.translatesAutoresizingMaskIntoConstraints = NO;
    [self.view addSubview:panel];

    UILabel *title = [UILabel new];
    title.text = @"Mod Văn Tuấn";
    title.textColor = [UIColor whiteColor];
    title.font = [UIFont boldSystemFontOfSize:22];
    title.textAlignment = NSTextAlignmentCenter;
    title.translatesAutoresizingMaskIntoConstraints = NO;
    [panel addSubview:title];

    UILabel *status = [UILabel new];
    status.text = @"Đang nhận mã thiết bị…";
    status.textColor = [UIColor lightGrayColor];
    status.font = [UIFont systemFontOfSize:13];
    status.textAlignment = NSTextAlignmentCenter;
    status.numberOfLines = 2;
    status.translatesAutoresizingMaskIntoConstraints = NO;
    [panel addSubview:status];
    self.statusLabel = status;

    UITextField *input = [UITextField new];
    input.placeholder = @"Điền key VTUAN vào đây";
    input.borderStyle = UITextBorderStyleRoundedRect;
    input.autocorrectionType = UITextAutocorrectionTypeNo;
    input.autocapitalizationType = UITextAutocapitalizationTypeAllCharacters;
    input.clearButtonMode = UITextFieldViewModeWhileEditing;
    input.returnKeyType = UIReturnKeyDone;
    input.delegate = self;
    input.enabled = NO;
    input.translatesAutoresizingMaskIntoConstraints = NO;
    [panel addSubview:input];
    self.keyField = input;

    UIButton *button = [UIButton buttonWithType:UIButtonTypeSystem];
    [button setTitle:@"Xác nhận" forState:UIControlStateNormal];
    [button setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    button.backgroundColor = [UIColor colorWithRed:0.15 green:0.48 blue:0.92 alpha:1];
    button.layer.cornerRadius = 8;
    button.enabled = NO;
    button.translatesAutoresizingMaskIntoConstraints = NO;
    [button addTarget:self action:@selector(submit:) forControlEvents:UIControlEventTouchUpInside];
    [panel addSubview:button];
    self.submitButton = button;

    NSLayoutConstraint *width = [panel.widthAnchor constraintEqualToAnchor:self.view.widthAnchor multiplier:0.85];
    width.priority = UILayoutPriorityDefaultHigh;
    [NSLayoutConstraint activateConstraints:@[
        [panel.centerXAnchor constraintEqualToAnchor:self.view.centerXAnchor],
        [panel.centerYAnchor constraintEqualToAnchor:self.view.centerYAnchor],
        [panel.widthAnchor constraintLessThanOrEqualToConstant:370],
        width,
        [panel.heightAnchor constraintEqualToConstant:228],
        [title.topAnchor constraintEqualToAnchor:panel.topAnchor constant:16],
        [title.leadingAnchor constraintEqualToAnchor:panel.leadingAnchor constant:12],
        [title.trailingAnchor constraintEqualToAnchor:panel.trailingAnchor constant:-12],
        [status.topAnchor constraintEqualToAnchor:title.bottomAnchor constant:12],
        [status.leadingAnchor constraintEqualToAnchor:panel.leadingAnchor constant:12],
        [status.trailingAnchor constraintEqualToAnchor:panel.trailingAnchor constant:-12],
        [status.heightAnchor constraintEqualToConstant:34],
        [input.topAnchor constraintEqualToAnchor:status.bottomAnchor constant:8],
        [input.leadingAnchor constraintEqualToAnchor:panel.leadingAnchor constant:16],
        [input.trailingAnchor constraintEqualToAnchor:panel.trailingAnchor constant:-16],
        [input.heightAnchor constraintEqualToConstant:38],
        [button.topAnchor constraintEqualToAnchor:input.bottomAnchor constant:12],
        [button.leadingAnchor constraintEqualToAnchor:input.leadingAnchor],
        [button.trailingAnchor constraintEqualToAnchor:input.trailingAnchor],
        [button.heightAnchor constraintEqualToConstant:38]
    ]];

    dispatch_async(dispatch_get_main_queue(), ^{
        self.device = vtDeviceID();
        if (!self.device.length) {
            self.statusLabel.text = @"Không lưu được mã thiết bị.";
            return;
        }
        NSLog(@"[VTUAN_KEYGATE] device ready");
        self.statusLabel.text = @"Đã nhận mã máy. Nhập key để kích hoạt.";
        self.keyField.enabled = self.submitButton.enabled = YES;
        NSString *saved = vtRead(kKey);
        if (saved.length) {
            self.keyField.text = saved;
            [self verify:saved];
        }
    });
}

- (BOOL)textFieldShouldReturn:(UITextField *)field {
    [field resignFirstResponder];
    [self submit:nil];
    return YES;
}
- (void)setBusyState:(BOOL)busy {
    self.busy = busy;
    self.keyField.enabled = self.submitButton.enabled = !busy;
}
- (void)submit:(id)sender {
    if (self.busy || !self.device.length) return;
    NSString *key = [[self.keyField.text stringByTrimmingCharactersInSet:
                      [NSCharacterSet whitespaceAndNewlineCharacterSet]] uppercaseString];
    if (!key.length) {
        self.statusLabel.text = @"Vui lòng nhập key.";
        return;
    }
    [self.keyField resignFirstResponder];
    [self setBusyState:YES];
    self.statusLabel.text = @"Đang kích hoạt key…";
    vtPost(@"/api/v1/activate", key, self.device, ^(BOOL ok, NSDictionary *json, NSString *message) {
        if (!ok) {
            self.statusLabel.text = message;
            [self setBusyState:NO];
            return;
        }
        [self verify:key];
    });
}
- (void)verify:(NSString *)key {
    [self setBusyState:YES];
    self.statusLabel.text = @"Đang kiểm tra key…";
    vtPost(@"/api/v1/check", key, self.device, ^(BOOL ok, NSDictionary *json, NSString *message) {
        [self setBusyState:NO];
        if (!ok || ![json[@"authorized"] boolValue]) {
            self.statusLabel.text = ok ? @"Key chưa được cấp quyền." : message;
            return;
        }
        if (!vtWrite(kKey, key)) {
            self.statusLabel.text = @"Key hợp lệ nhưng không lưu được.";
            return;
        }
        NSLog(@"[VTUAN_KEYGATE] authorized=1");
        [self dismissViewControllerAnimated:YES completion:nil];
    });
}
@end

@interface VTBootstrap : NSObject
@property(nonatomic, assign) BOOL shown;
+ (instancetype)shared;
- (void)launch:(NSNotification *)note;
- (void)tryShow;
@end

@implementation VTBootstrap
+ (instancetype)shared {
    static VTBootstrap *instance;
    static dispatch_once_t once;
    dispatch_once(&once, ^{ instance = [VTBootstrap new]; });
    return instance;
}
- (void)launch:(NSNotification *)note {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)),
                   dispatch_get_main_queue(), ^{ [self tryShow]; });
}
- (void)tryShow {
    if (self.shown) return;
    UIApplication *app = [UIApplication sharedApplication];
    UIWindow *window = app.keyWindow;
    if (!window) {
        for (UIWindow *w in app.windows) {
            if (w.isKeyWindow) { window = w; break; }
        }
    }
    UIViewController *root = window.rootViewController;
    if (!root || !root.view.window) {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)),
                       dispatch_get_main_queue(), ^{ [self tryShow]; });
        return;
    }
    while (root.presentedViewController) root = root.presentedViewController;
    if ([root isKindOfClass:[VTKeyViewController class]]) return;
    self.shown = YES;
    VTKeyViewController *gate = [VTKeyViewController new];
    gate.modalPresentationStyle = UIModalPresentationOverFullScreen;
    [root presentViewController:gate animated:YES completion:nil];
    NSLog(@"[VTUAN_KEYGATE] shown");
}
@end

__attribute__((constructor))
static void vtInstall(void) {
    NSLog(@"[VTUAN_KEYGATE] dylib loaded");
    dispatch_async(dispatch_get_main_queue(), ^{
        VTBootstrap *b = [VTBootstrap shared];
        [[NSNotificationCenter defaultCenter] addObserver:b selector:@selector(launch:)
                                                     name:UIApplicationDidBecomeActiveNotification object:nil];
        [b launch:nil];
    });
}
