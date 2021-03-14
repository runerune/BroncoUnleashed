package bike.hackboy.bronco;

import android.util.Log;
import com.google.protobuf.UnknownFieldSet;

public class ObservableDashboard {
    private UnknownFieldSet state = null;
    private ChangeListener listener;

    public UnknownFieldSet getState() {
        Log.w("state", "get state");
        Log.w("state", state.toString());
        return state;
    }

    public void setState(UnknownFieldSet state) {
        this.state = state;
        if (listener != null) listener.onChange();
    }

    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    public interface ChangeListener {
        void onChange();
    }
}
