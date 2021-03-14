package bike.hackboy.bronco;

import android.util.Log;

import com.google.protobuf.UnknownFieldSet;

import java.util.Map;

public class ObservableDashboard {
    private Map<Integer, UnknownFieldSet.Field> state = null;
    private ChangeListener listener;

    public Object getState() {
        Log.w("state", "get state");
        Log.w("state", state.toString());
        return state;
    }

    public void setState(UnknownFieldSet state) {
        this.state = state.asMap();
        if (listener != null) listener.onChange();
    }

    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    public interface ChangeListener {
        void onChange();
    }
}
