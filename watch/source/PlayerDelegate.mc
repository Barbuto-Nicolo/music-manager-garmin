import Toybox.Lang;
import Toybox.WatchUi;

class PlayerDelegate extends WatchUi.BehaviorDelegate {

    var _model as PlayerModel;

    function initialize(model as PlayerModel) {
        BehaviorDelegate.initialize();
        _model = model;
    }

    // START
    function onSelect() as Boolean {
        PhoneLink.send("playpause");
        return true;
    }

    // UP
    function onPreviousPage() as Boolean {
        PhoneLink.send(_model.volumeMode ? "volup" : "next");
        return true;
    }

    // DOWN
    function onNextPage() as Boolean {
        PhoneLink.send(_model.volumeMode ? "voldown" : "prev");
        return true;
    }

    // Long-press UP: toggle Tracks / Volume
    function onMenu() as Boolean {
        _model.volumeMode = !_model.volumeMode;
        WatchUi.requestUpdate();
        return true;
    }
}
