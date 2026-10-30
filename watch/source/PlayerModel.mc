import Toybox.Lang;

// Phone player state, updated by messages from the Android app.
class PlayerModel {
    var title as String = "";
    var artist as String = "";
    var playing as Boolean = false;
    var volume as Number = -1;
    var connected as Boolean = false;
    var volumeMode as Boolean = false;
    var status as String = "";

    function update(data as Dictionary) as Void {
        connected = true;
        var t = data["title"];
        var a = data["artist"];
        var p = data["playing"];
        var v = data["vol"];
        title = (t instanceof String) ? t : "";
        artist = (a instanceof String) ? a : "";
        playing = (p instanceof Boolean) ? p : false;
        volume = (v instanceof Number) ? v : volume;
    }
}
