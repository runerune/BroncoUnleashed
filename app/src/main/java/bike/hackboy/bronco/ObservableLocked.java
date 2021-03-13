package bike.hackboy.bronco;

public class ObservableLocked {
    private boolean locked = true;
    private ChangeListener listener;

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        if (listener != null) listener.onChange();
    }

    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    public interface ChangeListener {
        void onChange();
    }
}
