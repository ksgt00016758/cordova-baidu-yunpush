exec = require('cordova/exec');

var FGPushNotification = function() {
this.registered = false;
};
FGPushNotification.prototype.register = function(param)
{
exec(fastgoPushNotification.successFn, fastgoPushNotification.failureFn, 'FGPushNotification', 'register', [param]);
};

FGPushNotification.prototype.successFn = function(info)
{
if(info){
fastgoPushNotification.registered = true;
cordova.fireDocumentEvent("cloudPushRegistered", info);
}
};

FGPushNotification.prototype.failureFn = function(info)
{
fastgoPushNotification.registered = false;
};

var fastgoPushNotification = new FGPushNotification();

module.exports = fastgoPushNotification;
            
