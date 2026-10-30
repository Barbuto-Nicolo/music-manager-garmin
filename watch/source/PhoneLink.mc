import Toybox.Application;
import Toybox.Communications;
import Toybox.Lang;
import Toybox.WatchUi;

class PhoneLinkListener extends Communications.ConnectionListener {
    function initialize() {
        ConnectionListener.initialize();
    }
    function onComplete() as Void {
        PhoneLink.report("sent");
    }
    function onError() as Void {
        PhoneLink.report("SEND ERROR");
    }
}

module PhoneLink {
    function report(text as String) as Void {
        var app = Application.getApp() as PhoneAudioApp;
        app.model.status = text;
        WatchUi.requestUpdate();
    }

    // cmd: playpause | next | prev | volup | voldown | sync
    function send(cmd as String) as Void {
        Communications.transmit({"cmd" => cmd}, null, new PhoneLinkListener());
    }
}
