package bike.hackboy.bronco;

import android.util.Log;

public class ObservableDashboard {
    private DashboardProto.Dashboard state = null;
    private ChangeListener listener;

    public DashboardProto.Dashboard getState() {
        return state;
    }

    public void setState(DashboardProto.Dashboard state) {
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
