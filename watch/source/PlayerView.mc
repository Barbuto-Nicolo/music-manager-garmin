import Toybox.Graphics;
import Toybox.Lang;
import Toybox.WatchUi;

class PlayerView extends WatchUi.View {

    var _model as PlayerModel;

    function initialize(model as PlayerModel) {
        View.initialize();
        _model = model;
    }

    function onUpdate(dc as Dc) as Void {
        var w = dc.getWidth();
        var h = dc.getHeight();
        var cx = w / 2;

        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_BLACK);
        dc.clear();

        var mode = _model.volumeMode ? "VOLUME" : "TRACKS";
        dc.setColor(Graphics.COLOR_BLUE, Graphics.COLOR_TRANSPARENT);
        dc.drawText(cx, h * 0.10, Graphics.FONT_XTINY, mode, Graphics.TEXT_JUSTIFY_CENTER);

        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_TRANSPARENT);
        if (!_model.connected) {
            dc.drawText(cx, h * 0.40, Graphics.FONT_SMALL, "Waiting for\nphone...", Graphics.TEXT_JUSTIFY_CENTER);
        } else {
            var title = _model.title.equals("") ? "No track" : _model.title;
            dc.drawText(cx, h * 0.28, Graphics.FONT_SMALL, title, Graphics.TEXT_JUSTIFY_CENTER);
            dc.setColor(Graphics.COLOR_LT_GRAY, Graphics.COLOR_TRANSPARENT);
            dc.drawText(cx, h * 0.45, Graphics.FONT_XTINY, _model.artist, Graphics.TEXT_JUSTIFY_CENTER);
            dc.setColor(_model.playing ? Graphics.COLOR_GREEN : Graphics.COLOR_ORANGE, Graphics.COLOR_TRANSPARENT);
            dc.drawText(cx, h * 0.57, Graphics.FONT_MEDIUM, _model.playing ? "PLAY" : "PAUSE", Graphics.TEXT_JUSTIFY_CENTER);
            if (_model.volume >= 0) {
                dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_TRANSPARENT);
                dc.drawText(cx, h * 0.72, Graphics.FONT_XTINY, "Vol " + _model.volume + "%", Graphics.TEXT_JUSTIFY_CENTER);
            }
        }

        dc.setColor(Graphics.COLOR_ORANGE, Graphics.COLOR_TRANSPARENT);
        dc.drawText(cx, h * 0.18, Graphics.FONT_XTINY, _model.status, Graphics.TEXT_JUSTIFY_CENTER);

        dc.setColor(Graphics.COLOR_DK_GRAY, Graphics.COLOR_TRANSPARENT);
        var hint = _model.volumeMode ? "UP vol+  DOWN vol-" : "UP next  DOWN prev";
        dc.drawText(cx, h * 0.84, Graphics.FONT_XTINY, hint, Graphics.TEXT_JUSTIFY_CENTER);
    }
}
