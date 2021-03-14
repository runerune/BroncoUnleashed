package bike.hackboy.bronco;

import com.google.protobuf.UnknownFieldSet;

public class ObservableDashboard {
    private Object state = new Object();
    private ChangeListener listener;

    public Object getState() {
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
