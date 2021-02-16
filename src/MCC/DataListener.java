package MCC;

public interface DataListener<T> {

    public void onDataChanged(SmartObject<T> resource, T updatedValue);

}